package io.sclera.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.sclera.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fasterxml.uuid.Generators;

import io.sclera.Repository.MyDevicesCompanyRepository;
import io.sclera.Repository.MyDevicesSensorAttributesRepository;
import io.sclera.Repository.MyDevicesSensorRepository;
import io.sclera.dto.touchscreen.SensorDTO;
import io.sclera.rabbitmq.RabbitmqService;
import io.sclera.sockets.SocketService;
import io.sclera.utils.MyDevicesUtils;

@Service
public class MyDevicesService {

	@Autowired
	MyDevicesCompanyRepository myDevicesCompanyRepository;
	
	@Autowired
	MyDevicesSensorRepository myDevicesSensorRepository;
	
	@Autowired
	MyDevicesSensorAttributesRepository myDevicesSensorAttributesRepository;
	
	@Autowired
	MyDevicesUtils myDevicesUtils;
	
	@Autowired
	DeviceService deviceService;
	
	@Autowired
	SocketService socketService;
	
	@Autowired
	ConditionsService conditionsService;
	
	@Autowired
	RabbitmqService rabbitmqService;
	
	@Autowired
	MeasuringInstrumentService measuringInstrumentService;
	
	public void startMyDevicesService()
	{
		try {
			myDevicesUtils.updateMyDevicesUnitsMap();
			myDevicesUtils.updateMyDevicesCategoriesMap();
		} catch (Exception e) {
			System.out.println("Error starting my devices service " + e);
			System.out.println(e);
		}
	}
	
	public void upsertMyDevicesCompany(String username, String vdmsid, MyDevicesCompanyDTO myDevicesCompany) {
		if(myDevicesCompany.getId() == null)
		{
			String id = Generators.timeBasedGenerator().generate().toString();
			
			myDevicesCompanyRepository.insertMyDevicesCompany(id, myDevicesCompany.getCompany_name(), myDevicesCompany.getLocation_name(), vdmsid);
		}
		else
		{
			myDevicesCompanyRepository.updateMyDevicesCompany(myDevicesCompany.getId(), myDevicesCompany.getCompany_name(), 
					myDevicesCompany.getLocation_name(), vdmsid);
		}
	}

	public void updateMyDevicesEventData(JSONObject myDevicesEventData) {
		
		try {
			System.out.println("Data " + myDevicesEventData);
			JSONObject eventData = myDevicesEventData.getJSONObject("event_data");
			JSONObject deviceType = myDevicesEventData.getJSONObject("device_type");
			JSONArray payload = eventData.getJSONArray("payload");
			System.out.println("Payload " + payload);
			String company_name = myDevicesEventData.getJSONObject("company").getString("name");
			String location_name = myDevicesEventData.getJSONObject("location").getString("name");
			
			String my_devices_company_id = myDevicesCompanyRepository.getMyDevicesCompanyIdByName(company_name, location_name);
			if(my_devices_company_id != null)
			{
				this.upsertMyDevicesGatewaysByEventData(my_devices_company_id, eventData);
				
				this.upsertMyDevicesSensorsByEventData(my_devices_company_id, eventData, deviceType);
				
				this.updateMyDevicesSensorAttributesByEventData(eventData, payload);
			}
			
			
		} catch (Exception e) {
			System.out.println("Error updating event data from mydevices " + e);
			System.out.println(e);
		}
	}

	public void upsertMyDevicesGatewaysByEventData(String my_devices_company_id, JSONObject eventData)
	{
		try {
			JSONArray gateways = eventData.getJSONArray("gateways");
			BigInteger last_seen = eventData.getBigInteger("timestamp");
			
			if(gateways != null)
			{
				for (int i = 0; i < gateways.size(); i++) {
		            JSONObject gateway = gateways.getJSONObject(i);
		            String id = gateway.getString("gweui");
		            
		            if (myDevicesSensorRepository.getMyDevicesSensorCountById(id) == 0) {
		            	myDevicesSensorRepository.insertMyDevicesGatewayByEventData(id, false, last_seen, 1, my_devices_company_id);
		            } else {
		            	myDevicesSensorRepository.updateMyDevicesSensorByEventData(id, last_seen);
		            }
		        }
			}
		} catch (Exception e) {
			System.out.println("Error updating gateway data received from my devices event " + e);
			System.out.println(e);
		}
	}
	
	public void upsertMyDevicesSensorsByEventData(String my_devices_company_id, JSONObject eventData, JSONObject deviceType)
	{
		try {
			String id = eventData.getString("hardware_id");
			BigInteger last_seen = eventData.getBigInteger("timestamp");
			String name = deviceType.getString("name");
			String model = deviceType.getString("model");
			String manufacturer = deviceType.getString("manufacturer");
			
			if (myDevicesSensorRepository.getMyDevicesSensorCountById(id) == 0) {
				myDevicesSensorRepository.insertMyDevicesSensorByEventData(id, name, model, manufacturer, false, last_seen, 2, my_devices_company_id);
			} else {
				myDevicesSensorRepository.updateMyDevicesSensorByEventData(id, last_seen);
			}
		} catch (Exception e) {
			System.out.println("Error updating sensor data received from my devices event " + e);
			System.out.println(e);
		}
	}
	
	public void updateMyDevicesSensorAttributesByEventData(JSONObject eventData, JSONArray payload)
	{
		try {
			System.out.println("Came inside " + payload);
			String my_devices_sensor_id = eventData.getString("hardware_id");
			Integer signal_strength = null;
			Integer battery = null;
			
			if(payload != null)
			{
				
				List<Map<String,Object>> rabbitmqMyDeviceSensors = new ArrayList<>();
				for (int i = 0; i < payload.size(); i++) {
					System.out.println("Came inside for");
					JSONObject sensorAttribute = payload.getJSONObject(i);
					String name = sensorAttribute.getString("name");
					String valueString = sensorAttribute.get("value").toString();
					Integer valueInteger = sensorAttribute.getInteger("value");
					
					
					if(!(name.equals("RSSI") || name.equals("SNR") || name.equals("Battery")))
					{ 
						String myDevicesUnit = sensorAttribute.getString("unit");
						String unit = myDevicesUtils.getMyDevicesUnit(myDevicesUnit);
						
						String myDeviceCategory = sensorAttribute.getString("type");
						String category = myDevicesUtils.getMyDevicesCategory(myDeviceCategory);
						
						if(unit == null)
						{
							unit = myDevicesUnit;
						}
						
						BigInteger timestamp = sensorAttribute.getBigInteger("timestamp");
						
						try {
							Map<String, Object> rabbitmqMyDeviceSensor = new HashMap<>();
							 rabbitmqMyDeviceSensor.put("protocol", "my_devices");
							 rabbitmqMyDeviceSensor.put("primary_id", name);
							 rabbitmqMyDeviceSensor.put("secondary_id", my_devices_sensor_id);
							 rabbitmqMyDeviceSensor.put("value", valueString);
							 rabbitmqMyDeviceSensor.put("timestamp", timestamp);
							rabbitmqMyDeviceSensor.put("category", category);
							 rabbitmqMyDeviceSensors.add(rabbitmqMyDeviceSensor);
						} catch (Exception e) {
							// TODO: handle exception
						}
						 
						if (myDevicesSensorAttributesRepository.getMyDevicesSensorAttributesCountById(name, my_devices_sensor_id) == 0) {
							myDevicesSensorAttributesRepository.insertMyDevicesSensorAttributesByEventData(name, my_devices_sensor_id, valueString, unit, timestamp, category);
						} else {
							myDevicesSensorAttributesRepository.updateMyDevicesSensorAttributesByEventData(name, my_devices_sensor_id, valueString, timestamp);
						}
						
						try {
							conditionsService.updateConditionAlert("my_devices", my_devices_sensor_id, name, valueString, "", "sync");
							socketService.socketMyDevicesValueUpdate(my_devices_sensor_id);
						} catch (Exception e) {
							System.out.println("Error updating my devices sensor attributes conditions or socket event " + e);
						}
						
						try {
							System.out.println("EVENTT");
							measuringInstrumentService.updateMeasuringIntrumentParametersByIds("my_devices", name, my_devices_sensor_id , valueString);
						} catch (Exception e) {
							System.out.println("Error updating measuring instrument " + e);
						}
						
					}
					
					if(name.equals("RSSI"))
					{
						System.out.println("Came inside for rssi"); 
						signal_strength = myDevicesUtils.getPercentage(valueInteger, -120, -15);
					}
					
					if(name.equals("Battery"))
					{
						System.out.println("Came inside for battery"); 
						battery = valueInteger;
					}
				}
				
			myDevicesSensorRepository.updateMyDevicesSensorBatteryAndSignalStrength(my_devices_sensor_id, signal_strength, battery);
			try {
				rabbitmqService.rabbitmqMyDevicesData(rabbitmqMyDeviceSensors);
			} catch (Exception e) {
				// TODO: handle exception
			}
		
			}
		} catch (Exception e) {
			System.out.println("Error updating sensor attributes data received from my devices event " + e);
			System.out.println(e);
		}
	}

	//to be removed after pagination api works
	public List<MyDevicesCompanyDTO> getAllMyDevicesCompanies(String username, String vdmsid) {
		List<MyDevicesCompanyDTO> myDevicesCompanies = this.getAllMyDevicesCompanyByVdmsId(vdmsid);
		for(MyDevicesCompanyDTO myDevicesCompany : myDevicesCompanies)
		{
			List<MyDevicesSensorDTO> myDevicesSensors = this.getAllMyDevicesSensorsByCompanyId(myDevicesCompany.getId());
			for(MyDevicesSensorDTO myDevicesSensor : myDevicesSensors)
			{
				myDevicesSensor.setMy_devices_sensor_attributes(this.getAllMyDevicesSensorAttributesBySensorId(myDevicesSensor.getId()));
			}
			myDevicesCompany.setMy_devices_sensor(myDevicesSensors);	
		}
		return myDevicesCompanies;
	}
	
	//Added pagination for getAllMyDevicesCompanies
	public Set<MyDevicesCompanyDTO> getMyDevicesCompaniesPagination(String username, String vdmsid, String searchkey,Integer pageno,Integer pagesize) {

		Integer offset=pagesize * (pageno-1);

		Set<MyDevicesCompanyDTO> myDevicesCompany = myDevicesCompanyRepository.getMyDevicesCompaniesPagination(searchkey,pagesize,offset);

		return myDevicesCompany;
	}
	
	
	public List<MyDevicesCompanyDTO> getAllMyDevicesCompanyByVdmsId(String vdmsid)
	{
		return myDevicesCompanyRepository.getAllMyDevicesCompanyByVdmsId(vdmsid);
	}
	
	//to be removed after pagination api works
	public List<MyDevicesSensorDTO> getAllMyDevicesSensorsByCompanyId(String my_devices_company_id)
	{
		return myDevicesSensorRepository.getAllMyDevicesSensorsByCompanyId(my_devices_company_id);
	}
	
	//Added pagination for getAllMyDevicesSensorsByCompanyId
	public Set<MyDevicesSensorDTO> getMyDevicesSensorsByPagination(String username, String vdmsid, String company_id, String searchakey, Integer pageno,
			Integer pagesize) {

		Integer offset = pagesize * (pageno - 1);

		Set<MyDevicesSensorDTO> myDevicesSensors = this.getMyDevicesSensorsByCompanyIdPagination(company_id, searchakey, pagesize, offset);

		for(MyDevicesSensorDTO myDevicesSensor : myDevicesSensors)
		{
			myDevicesSensor.setMy_devices_sensor_attributes(this.getAllMyDevicesSensorAttributesBySensorId(myDevicesSensor.getId()));
		}

		return myDevicesSensors;
	}
	
	//Get Mydevices sensors by compnay id pagination
	public Set<MyDevicesSensorDTO> getMyDevicesSensorsByCompanyIdPagination(String my_devices_company_id,String searchakey, Integer pagesize, Integer offset)
	{
		return myDevicesSensorRepository.getMyDevicesSensorsByCompanyIdPagination(my_devices_company_id, searchakey, pagesize, offset);
	}
	
	public List<MyDevicesSensorAttributesDTO> getAllMyDevicesSensorAttributesBySensorId(String my_devices_sensor_id)
	{
		return myDevicesSensorAttributesRepository.getAllMyDevicesSensorAttributesBySensorId(my_devices_sensor_id);
	}

	public void deleteMyDevicesCompany(String username, String vdmsid, String my_devices_company_id) {
		
		List<MyDevicesSensorDTO> myDevicesSensors = this.getAllMyDevicesSensorsByCompanyId(my_devices_company_id);
		
		myDevicesCompanyRepository.deleteById(my_devices_company_id);
		
		//Device My Devices Count and status
		try {
			for(MyDevicesSensorDTO myDevicesSensor : myDevicesSensors)
			{
				String device_id = myDevicesSensor.getDevice_id();
				deviceService.updateDeviceMyDevicesCountByDeviceId(device_id);
				deviceService.updateDeviceMyDevicesStatusByDeviceId(device_id);
			}
		} catch (Exception e) {
			System.out.println("Error Updating device my devices count and status in delete my device company " + e);
		}
	}

	public MyDevicesSensorDTO getMyDevicesSensor(String username, String vdmsid, String my_devices_sensor_id) {
		MyDevicesSensorDTO myDevicesSensor = this.getMyDevicesSensorById(my_devices_sensor_id);
		myDevicesSensor.setMy_devices_sensor_attributes(this.getAllMyDevicesSensorAttributesBySensorId(my_devices_sensor_id));
		return myDevicesSensor;
	}
	
	public MyDevicesSensorDTO getMyDevicesSensorById(String my_devices_sensor_id) {
		return myDevicesSensorRepository.getMyDevicesSensorById(my_devices_sensor_id);
	}

	public Set<MyDevicesSensorDTO> getDeviceMyDevicesSensors(String username, String vdmsid, String device_id) {
		Set<MyDevicesSensorDTO> myDevicesSensors = this.getMyDevicesSensorsByDeviceId(device_id);
		
		for(MyDevicesSensorDTO myDevicesSensor : myDevicesSensors)
		{
			myDevicesSensor.setMy_devices_sensor_attributes(this.getAllMyDevicesSensorAttributesBySensorId(myDevicesSensor.getId()));
		}
		
		return myDevicesSensors;
	}
	
	public Set<MyDevicesSensorDTO> getMyDevicesSensorsByDeviceId(String device_id) {
		return myDevicesSensorRepository.getMyDevicesSensorByDeviceId(device_id);
	}

	public void updateDeviceMyDevicesSensors(String username, String vdmsid,
			List<MyDevicesSensorDTO> myDevicesSensors) {
		for(MyDevicesSensorDTO myDevicesSensor : myDevicesSensors)
		{
			myDevicesSensorRepository.updateDeviceMyDevicesSensor(myDevicesSensor.getId(), myDevicesSensor.getDevice_id());
			//update device my device sensor count
			deviceService.updateDeviceMyDevicesCountByDeviceId(myDevicesSensor.getDevice_id());
			//update device my device alert status
			deviceService.updateDeviceMyDevicesStatusByDeviceId(myDevicesSensor.getDevice_id());
		}
	}

	public void deleteDeviceMyDevicesSensors(String username, String vdmsid,
			List<MyDevicesSensorDTO> myDevicesSensors) {
		for(MyDevicesSensorDTO myDevicesSensor : myDevicesSensors)
		{
			myDevicesSensorRepository.deleteDeviceMyDevicesSensor(myDevicesSensor.getId());
			//update device my device sensor count
			deviceService.updateDeviceMyDevicesCountByDeviceId(myDevicesSensor.getDevice_id());
			//update device my device alert status
			deviceService.updateDeviceMyDevicesStatusByDeviceId(myDevicesSensor.getDevice_id());
		}
	}

	//to be removed after pagination api works
	public List<MyDevicesSensorDTO> getAllMyDevicesSensors(String username, String vdmsid) {
		
		List<MyDevicesSensorDTO> myDevicesSensors = this.getConfiguredMyDevicesSensors();

		for(MyDevicesSensorDTO myDevicesSensor : myDevicesSensors)
		{
			myDevicesSensor.setMy_devices_sensor_attributes(this.getAllMyDevicesSensorAttributesBySensorId(myDevicesSensor.getId()));
		}
		
		return myDevicesSensors;
	}
	
	//Added pagination for getAllMyDevicesSensors
	public List<MyDevicesSensorDTO> getAllMyDevicesSensorsByPagination(String username, String vdmsid, String searchkey, Integer pageno, Integer pagesize) {

		Integer offset=pagesize * (pageno-1);

		List<MyDevicesSensorDTO> myDevicesSensors = myDevicesSensorRepository.getAllMyDevicesSensorsByPagination(searchkey, pagesize, offset);

		for(MyDevicesSensorDTO myDevicesSensor : myDevicesSensors)
		{
			myDevicesSensor.setMy_devices_sensor_attributes(this.getAllMyDevicesSensorAttributesBySensorId(myDevicesSensor.getId()));
		}

		return myDevicesSensors;
	}
	
	public List<MyDevicesSensorDTO> getConfiguredMyDevicesSensors() {
		return myDevicesSensorRepository.getConfiguredMyDevicesSensors();
	}

	public void updateMyDevicesSensors(String username, String vdmsid, List<MyDevicesSensorDTO> myDevicesSensors) {
		for(MyDevicesSensorDTO myDevicesSensor : myDevicesSensors)
		{
			myDevicesSensorRepository.updateMyDevicesSensor(myDevicesSensor.getId(), myDevicesSensor.getUser_data_name(), 
					myDevicesSensor.getConfiguration(), myDevicesSensor.getSensor_type());
		}
	}

	public void deleteMyDevicesSensors(String username, String vdmsid, List<MyDevicesSensorDTO> myDevicesSensors) {
		for(MyDevicesSensorDTO myDevicesSensor : myDevicesSensors)
		{
			String device_id = this.getDeviceIdByMyDevicesSensorId(myDevicesSensor.getId());
			
			myDevicesSensorRepository.deleteById(myDevicesSensor.getId());
			
			//Device My Devices Count and status
			try {
				deviceService.updateDeviceMyDevicesCountByDeviceId(device_id);
				deviceService.updateDeviceMyDevicesStatusByDeviceId(device_id);
			} catch (Exception e) {
				System.out.println("Error Updating device my devices count and status in delete " + e);
			}
		}
	}

	public void updateMyDevicesSensorAttributesAlert(String my_devices_sensor_id,
			String my_devices_sensor_attributes_name, Boolean newAlert) {
		myDevicesSensorAttributesRepository.updateMyDevicesSensorAttributesAlert(my_devices_sensor_id, my_devices_sensor_attributes_name, newAlert);
	}
	
	public void updateMyDevicesSensorAlert(String my_devices_sensor_id) {
		List<MyDevicesSensorAttributesDTO> myDevicesSensorAttributes = this.getAllMyDevicesSensorAttributesBySensorId(my_devices_sensor_id);
		boolean alert = false;
		for(MyDevicesSensorAttributesDTO myDevicesSensorAttribute : myDevicesSensorAttributes)
		{
			if(myDevicesSensorAttribute.getAlert())
			{
				alert = true;
				break;
			}
		}
		myDevicesSensorRepository.updateMyDevicesSensorAlert(my_devices_sensor_id, alert);
	}

	public void updateMyDevicesSensorAttributesUserDataValue(String my_devices_sensor_id,
			String my_devices_sensor_attributes_name, String user_data_value) {
		myDevicesSensorAttributesRepository.updateMyDevicesSensorAttributesUserDataValue(my_devices_sensor_id, my_devices_sensor_attributes_name, user_data_value);
	}
	
	public String getDeviceIdByMyDevicesSensorId(String my_devices_sensor_id) {
		return myDevicesSensorRepository.getDeviceIdByMyDevicesSensorId(my_devices_sensor_id);
	}
	
	public MyDevicesSensorDTO getMyDevicesSensorByIdSocket(String my_devices_sensor_id) {
		MyDevicesSensorDTO myDeviceSensor = myDevicesSensorRepository.getMyDevicesSensorByIdSocket(my_devices_sensor_id);
		
		myDeviceSensor.setMy_devices_sensor_attributes(this.getAllMyDevicesSensorAttributesBySensorId(my_devices_sensor_id));
		return myDeviceSensor;
	}
	
	//get my devices sensor by id for all required platforms
	public MyDevicesSensorDetailsDTO getMyDevicesSensorDetailsById(String my_devices_sensor_id) {
		MyDevicesSensorDetailsDTO myDevicesSensorDetails = myDevicesSensorRepository.getMyDevicesSensorDetailsById(my_devices_sensor_id);
		myDevicesSensorDetails.setMy_devices_sensor_attributes(this.getAllMyDevicesSensorAttributesBySensorId(my_devices_sensor_id));

		return myDevicesSensorDetails;
	}
	
	//get mydevices sensor count for TS pagination
	public Integer getMyDevicesSensorsCountTS(String networkname, String buildingid, String floorid,String locationid, Integer status) {
		return myDevicesSensorRepository.getMyDevicesSensorsCountTS(buildingid, floorid, locationid, status);
	}
	
	//Touchscreen API
	
	//To be removed after pagination api works
	public List<MyDevicesSensorDTO> listMyDevicesSensorsTS(String networkname, String buildingid, String floorid,
			String locationid, Integer status) {
		List<MyDevicesSensorDTO> myDeviceSensors = myDevicesSensorRepository.listMyDevicesSensorsTS(buildingid, floorid, locationid, status);
		for(MyDevicesSensorDTO myDeviceSensor : myDeviceSensors)
		{
			myDeviceSensor.setMy_devices_sensor_attributes(this.getAllMyDevicesSensorAttributesBySensorId(myDeviceSensor.getId()));
		}
		return myDeviceSensors;
	}

	//added pagination for listMyDevicesSensorsTS
	public Set<MyDevicesSensorDTO> listMyDevicesSensorsPaginationTS(String networkname, String buildingid, String floorid,
			String locationid, Integer status, Integer pagesize, Integer offset) {
		Set<MyDevicesSensorDTO> myDeviceSensors = myDevicesSensorRepository.listMyDevicesSensorsPaginationTS(buildingid, floorid, locationid, status, pagesize, offset);
		for(MyDevicesSensorDTO myDeviceSensor : myDeviceSensors)
		{
			myDeviceSensor.setMy_devices_sensor_attributes(this.getAllMyDevicesSensorAttributesBySensorId(myDeviceSensor.getId()));
		}
		return myDeviceSensors;
	}
	
	public Map<String, Integer> getmyDevicesSensorAlertsCount() {
		Map<String, Integer> myDevicesAlertCount = new HashMap<>();
		
		myDevicesAlertCount.put("my_devices_sensor_with_alert_count", myDevicesSensorRepository.getmyDevicesSensorAlertCount(true));
		myDevicesAlertCount.put("my_devices_sensor_without_alert_count", myDevicesSensorRepository.getmyDevicesSensorAlertCount(false));
		
		return myDevicesAlertCount;
	}

	public Integer getMyDevicesSensorCountByDeviceId(String device_id) {
		return myDevicesSensorRepository.getMyDevicesSensorCountByDeviceId(device_id);
	}

	public Boolean getMyDevicesSensorAlertStatusByDeviceId(String device_id) {
		Integer my_devices_alert_count = myDevicesSensorRepository.getMyDevicesSensorAlertCountByDeviceId(device_id, true);
		if(my_devices_alert_count > 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public AlertDTO getMyDevicesAlertInfoById(String my_devices_sensor_id, String my_devices_sensor_attributes_name) {
		return myDevicesSensorAttributesRepository.getMyDevicesAlertInfoById(my_devices_sensor_id, my_devices_sensor_attributes_name);
	}

	
	
//	public List<SensorDTO> getMyDevicesSensorsByDeviceIdTS(String device_id) {
//		List<SensorDTO> myDevicesSensors = myDevicesSensorRepository.getMyDevicesSensorsByDeviceIdTS(device_id);
//
//		for(SensorDTO myDevicesSensor : myDevicesSensors)
//		{
//			myDevicesSensor.setSensor_values(myDevicesSensorAttributesRepository.getmyDevicesSensorAttributeValuesByIdTS(myDevicesSensor.getPrimary_id()));
//		}
//
//		return myDevicesSensors;
//	}

	public List<SensorDTO> getMydevicesSensorsByDeviceId(String device_id) {

		return myDevicesSensorAttributesRepository.getMyDevicesSensorAttributeByDeviceId(device_id);

	}


	//Get My Devices Sensor Attribute Current Value
	public String getMyDevicesSensorAttributeCurrentValue(String my_devices_sensor_id,
			String my_devices_sensor_attributes_name) {
		return  myDevicesSensorAttributesRepository.getMyDevicesSensorAttributeCurrentValue(my_devices_sensor_id,my_devices_sensor_attributes_name);
	}

	
	public Set<String> getUniqueSensorCategoryByFloor(String floorid) {
		// TODO Auto-generated method stub
		return myDevicesSensorAttributesRepository.getUniqueSensorCategoryByFloor(floorid);
	}

	public Set<CategorySensorDTO> getSensorCategoryByFloor(String floorid, String category) {
		// TODO Auto-generated method stub
		return myDevicesSensorAttributesRepository.getSensorCategoryByFloor(floorid, category);
	}

	public Integer getSensorCategoryByFloorCount(String floorid, String category) {
		return myDevicesSensorAttributesRepository.getSensorCategoryByFloorCount(floorid, category);

	}

	public List<CategorySensorDTO> getSensorCategoryByFloorPagination(String floorid, String category, Integer pagesize, Integer offset) {
		return myDevicesSensorAttributesRepository.getSensorCategoryByFloorPagination(floorid, category, pagesize, offset);
	}

	//get mydevices sensor alert status count
	public Map<String, Integer> getmyDevicesSensorAlertStatusCountTS(String networkname, String buildingid, String floorid, String locationid) {
		Map<String, Integer> myDevicesAlertCountStatus = new HashMap<>();

		myDevicesAlertCountStatus.put("my_devices_sensor_with_alert_count", myDevicesSensorRepository.getmyDevicesSensorAlertStatusCountTS(true, networkname, buildingid, floorid, locationid));
		myDevicesAlertCountStatus.put("my_devices_sensor_without_alert_count", myDevicesSensorRepository.getmyDevicesSensorAlertStatusCountTS(false, networkname, buildingid, floorid, locationid));

		return myDevicesAlertCountStatus;
	}

	public void updateMyDevicesSensorAttributes(String username, String vdmsid, List<MyDevicesSensorAttributesDTO> myDevicesSensorAttributes) {
		for(MyDevicesSensorAttributesDTO myDevicesSensorAttribute : myDevicesSensorAttributes)
		{
			myDevicesSensorAttributesRepository.updateMyDevicesSensorAttributes(myDevicesSensorAttribute.getMy_devices_sensor_id(),
					myDevicesSensorAttribute.getName(),myDevicesSensorAttribute.getShow_on_map(), myDevicesSensorAttribute.getShow_on_scan());
		}
	}


	public Set<AnalyticSensorDTO> getAnalyticMyDevicesSensorAttributes(String category, String searchkey, Integer pageno, Integer pagesize, String report_template_id) {
		Integer offset = pagesize * (pageno - 1);
		return myDevicesSensorAttributesRepository.getAnalyticMyDevicesSensorAttributes(category, searchkey, pagesize, offset, report_template_id);
	}

	public List<ConditionsDTO> listmydevicesDeviceAlertMessagesByDeviceIds(List<String> ids) {
		return myDevicesSensorRepository.listmydevicesDeviceAlertMessagesByDevice(ids);
	}

    public SensorAlertDTO getMyDevicesSensorAttributesAlertDetails(String my_devices_sensor_id, String my_devices_sensor_attributes_name) {
		return myDevicesSensorAttributesRepository.getMyDevicesSensorAttributesAlertDetails(my_devices_sensor_id, my_devices_sensor_attributes_name);
    }

	public Set<SensorDTO> getSensorByDeviceId(String deviceid) {
		// TODO Auto-generated method stub
		return myDevicesSensorAttributesRepository.getSensorByDeviceId(deviceid);
	}

	public Set<SensorDTO> getSensorByLocationId(String locationid) {
		// TODO Auto-generated method stub
		return myDevicesSensorAttributesRepository.getSensorByLocationId(locationid);
	}

	public List<CategorySensorDTO> getSensorCategoryByLocationPagination(String locationid, String category, Integer pagesize, Integer offset) {
		return myDevicesSensorAttributesRepository.getSensorCategoryByLocationPagination(locationid,category,pagesize,offset);
	}

	public Integer getSensorCategoryByLocationCount(String locationid, String category) {
		return myDevicesSensorAttributesRepository.getSensorCategoryByLocationCount(locationid, category);

	}

    public AnalyticSensorDTO getMyDevicesSensorByTemplateId(String my_devices_sensor_attributes_name, String my_devices_sensor_id, String searchkey,String report_attribute_id) {
		return myDevicesSensorAttributesRepository.getMyDevicesSensorByTemplateId(my_devices_sensor_attributes_name, my_devices_sensor_id, searchkey,report_attribute_id);
    }


    public Set<SensorDTO> getIntegrationSensorByLocationId(String locationid) {
		return myDevicesSensorAttributesRepository.getIntegrationSensorByLocationId(locationid);
    }

	public void updateMyDevicesSensorDeviceId(String device_id, String existing_device_id, Set<String> retainDevices) {
		myDevicesSensorRepository.updateMyDevicesSensorDeviceId(device_id, existing_device_id);
		deviceService.updateDeviceMyDevicesCountByDeviceId(device_id);
		deviceService.updateDeviceMyDevicesStatusByDeviceId(device_id);
		if (!retainDevices.isEmpty() && retainDevices.contains(existing_device_id)) {
			deviceService.updateDeviceMyDevicesCountByDeviceId(existing_device_id);
			deviceService.updateDeviceMyDevicesStatusByDeviceId(existing_device_id);
		}
	}
}
