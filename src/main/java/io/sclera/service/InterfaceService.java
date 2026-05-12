package io.sclera.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.uuid.Generators;

import io.sclera.Repository.InterfaceRepository;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.InterfaceDTO;
import io.sclera.dto.touchscreen.DeviceMonitorDTO;
import io.sclera.dto.touchscreen.SnmpInterfaceDTO;
import io.sclera.dto.touchscreen.SnmpValuesDTO;
import io.sclera.sockets.SocketService;

@Service
public class InterfaceService {

	@Autowired
	InterfaceRepository interfaceRepository;

	@Autowired
	DockerService dockerService;

	@Autowired
	DeviceService deviceService;

	@Autowired 
	APICallService apiCallService;
	
	@Autowired
	SnmpService snmpService;
	
	@Autowired
	SocketService socketService;

	public Integer getInterfaceCountByDevice(String id) {
		return interfaceRepository.getInterfaceCountByDevice(id);
	}


	
	//IP conflict is pending
	public void listDeviceSnmpInterfaceByDeviceIdFromDevice(String vdms_id, String docker_name, String device_id) {
		//		String internal_Ip_address = dockerService.getInternalIPbyDockername(vdms_id, docker_name);
		String internal_Ip_address = dockerService.getDockerInternalIp(docker_name);

		SnmpValuesDTO device = deviceService.getDeviceSnmpByDeviceId(docker_name, device_id);
		device = snmpService.validateSnmpParameters(device);

		if(device != null) {
			List<InterfaceDTO> interfaces = apiCallService.syncSnmpInterfacebyDeviceId(internal_Ip_address, device);
			if(interfaces != null && interfaces.size() > 0) {
				List<InterfaceDTO> interfaces_db_list = interfaceRepository.listDeviceInterfaceByDeviceId(device_id);
				if(interfaces_db_list != null && interfaces_db_list.size() > 0) {
					for (InterfaceDTO snmp_interface : interfaces) {
						try {
							interfaceRepository.updateRequiredInterfaceById(snmp_interface.getName(), snmp_interface.getDownload(), snmp_interface.getUpload(), 
									snmp_interface.getStatus(), snmp_interface.getMac_address(), snmp_interface.getCategory(), snmp_interface.getType(), snmp_interface.getIfindex(), 
									snmp_interface.getSpeed(), null, null, device_id, null);
						} catch (Exception e) {
							System.out.println(e);
						}
					}
				}
			}
		}
	}


	public List<InterfaceDTO> sortInterfaceListByIfindex(List<InterfaceDTO> interfaces){
		if(interfaces.size() > 0) {
			Comparator<InterfaceDTO> compareById = (InterfaceDTO o1, InterfaceDTO o2) -> Integer.decode(o1.getIfindex()).compareTo( Integer.decode(o2.getIfindex()) );
			Collections.sort(interfaces, compareById);
			return interfaces;
		}
		return null;
	}

//	public void updateDeviceInterfaces(SnmpValuesDTO snmpInterface) {
//
//		try {
//			String device_id = snmpInterface.getId();
//
//			if (snmpInterface.getInterfaces().size() > 0) {
//				List<InterfaceDTO> interfaceList = snmpInterface.getInterfaces();
//				List<InterfaceDTO> allInterfaces = interfaceRepository.listDeviceInterfaceByDeviceId(device_id);
//
//				System.out.println("Interface DTO Size :" + allInterfaces.size());
//				System.out.println("device id :" + device_id);
//				List<String> interfacesIndex = new ArrayList<>();
//				for (int i = 0; i < allInterfaces.size(); i++) {
//					interfacesIndex.add(allInterfaces.get(i).getIfindex());
//				}
//
//				List<String> interfaceListIndex = new ArrayList<>();
//				for (int i = 0; i < interfaceList.size(); i++) {
//					interfaceListIndex.add(interfaceList.get(i).getIfindex());
//				}
//
//				System.out.println("DB count :" + interfacesIndex.size());
//				System.out.println("list count :" + interfaceList.size());
//				if (allInterfaces.size() > 0) {
//
//					for (InterfaceDTO interfacedto : interfaceList) {
//						try {
//							if(interfacedto.getConnected_devices() != null && interfacedto.getConnected_devices().size() > 0) {
//								String connectedDeviceIdString = apiCallService.getJSONStringFromJsonArray(interfacedto.getConnected_devices());//convert List<String> to Json String
//								interfacedto.setConnected_devices_json(connectedDeviceIdString);
//							}
//
//							if (!interfacesIndex.contains(interfacedto.getIfindex())) {
//								String id = Generators.timeBasedGenerator().generate().toString();
//								interfaceRepository.createInterfaceById(id, interfacedto.getName(), interfacedto.getDownload(),
//										interfacedto.getUpload(), interfacedto.getStatus(), interfacedto.getMac_address(),
//										interfacedto.getCategory(), interfacedto.getType(), interfacedto.getIfindex(),interfacedto.getSpeed(), 
//										interfacedto.getConnected_devices_json(), interfacedto.getUser_connected_devices_json(), device_id, interfacedto.getUplink());
//							} else {
//								interfaceRepository.updateInterfaceById(interfacedto.getName(), interfacedto.getDownload(),
//										interfacedto.getUpload(), interfacedto.getStatus(), interfacedto.getMac_address(),
//										interfacedto.getCategory(), interfacedto.getType(), interfacedto.getIfindex(),interfacedto.getSpeed(), 
//										interfacedto.getConnected_devices_json(), interfacedto.getUser_connected_devices_json(), device_id, interfacedto.getUplink());
//							}
//						} catch (Exception e) {
//							// TODO: handle exception
//						}
//
//					}
//
//					for (int i = 0; i < allInterfaces.size(); i++) {
//						try {
//							if (!interfaceListIndex.contains(allInterfaces.get(i).getIfindex())) {
//								interfaceRepository.deleteByIndex(allInterfaces.get(i).getIfindex(), device_id);
//							}
//						} catch (Exception e) {
//							// TODO: handle exception
//						}
//
//					}
//
//
//				} else {
//					for (InterfaceDTO interfacedto : interfaceList) {
//						try {
//							if(interfacedto.getConnected_devices() != null && interfacedto.getConnected_devices().size() > 0) {
//								String connectedDeviceIdString = apiCallService.getJSONStringFromJsonArray(interfacedto.getConnected_devices());//convert List<String> to Json String
//								interfacedto.setConnected_devices_json(connectedDeviceIdString);
//							}
//							String id = Generators.timeBasedGenerator().generate().toString();
//							interfaceRepository.createInterfaceById(id, interfacedto.getName(), interfacedto.getDownload(),
//									interfacedto.getUpload(), interfacedto.getStatus(), interfacedto.getMac_address(),
//									interfacedto.getCategory(), interfacedto.getType(), interfacedto.getIfindex(),interfacedto.getSpeed(), 
//									interfacedto.getConnected_devices_json(), interfacedto.getUser_connected_devices_json(), device_id, interfacedto.getUplink());
//
//						} catch (Exception e) {
//							// TODO: handle exception
//						}
//					}
//				}
//			}
//			try {
//				deviceService.updateDeviceInterfaceCount(device_id);
//			} catch (Exception e) {
//				System.out.println("Error updating device interface count " + e);
//
//			}
//		} catch (Exception e) {
//			System.out.println(e);
//		}
//
//
//	}

	public void updateDeviceInterfaces(SnmpValuesDTO snmpInterface) {

		try {
			String device_id = snmpInterface.getId();

			if (snmpInterface.getInterfaces().size() > 0) {
				List<InterfaceDTO> interfaceList = snmpInterface.getInterfaces();
				List<InterfaceDTO> allInterfaces = interfaceRepository.listDeviceInterfaceByDeviceId(device_id);

				System.out.println("Interface DTO Size :" + allInterfaces.size());
				System.out.println("device id :" + device_id);
				List<String> interfacesIndex = new ArrayList<>();
				for (InterfaceDTO allInterface : allInterfaces) {
					interfacesIndex.add(allInterface.getIfindex());
				}

				List<String> interfaceListIndex = new ArrayList<>();
				for (InterfaceDTO interfaceDTO : interfaceList) {
					interfaceListIndex.add(interfaceDTO.getIfindex());
				}

				System.out.println("DB count :" + interfacesIndex.size());
				System.out.println("list count :" + interfaceList.size());

				if (allInterfaces.size() > 0) {

					for (InterfaceDTO interfacedto : interfaceList) {
						JSONObject connectedDeviceJsonObject = new JSONObject();

						try {
							if ((interfacedto.getConnected_devices() != null && interfacedto.getConnected_devices().size() > 0) || (interfacedto.getConnected_mac_addresses() != null && interfacedto.getConnected_mac_addresses().size() > 0)) {
								connectedDeviceJsonObject.put("deviceId", interfacedto.getConnected_devices());
								connectedDeviceJsonObject.put("macs", interfacedto.getConnected_mac_addresses());
							}

							interfacedto.setConnected_devices_json(String.valueOf(connectedDeviceJsonObject));

							if (!interfacesIndex.contains(interfacedto.getIfindex())) {
								String id = Generators.timeBasedGenerator().generate().toString();
								interfaceRepository.createInterfaceById(id, interfacedto.getName(), interfacedto.getDownload(),
										interfacedto.getUpload(), interfacedto.getStatus(), interfacedto.getMac_address(),
										interfacedto.getCategory(), interfacedto.getType(), interfacedto.getIfindex(), interfacedto.getSpeed(),
										interfacedto.getConnected_devices_json(), interfacedto.getUser_connected_devices_json(), device_id, interfacedto.getUplink());
							} else {
								interfaceRepository.updateInterfaceById(interfacedto.getName(), interfacedto.getDownload(),
										interfacedto.getUpload(), interfacedto.getStatus(), interfacedto.getMac_address(),
										interfacedto.getCategory(), interfacedto.getType(), interfacedto.getIfindex(), interfacedto.getSpeed(),
										interfacedto.getConnected_devices_json(), interfacedto.getUser_connected_devices_json(), device_id, interfacedto.getUplink());
							}
						} catch (Exception e) {
							System.out.println("UPSERT INTERFACE IS FAILED IN UPDATE INTERFACE");
						}

					}

					for (int i = 0; i < allInterfaces.size(); i++) {
						try {
							if (!interfaceListIndex.contains(allInterfaces.get(i).getIfindex())) {
								interfaceRepository.deleteByIndex(allInterfaces.get(i).getIfindex(), device_id);
							}
						} catch (Exception e) {
							System.out.println("DELETE INTERFACE IS FAILED IN UPDATE INTERFACE");
						}

					}


				} else {

					for (InterfaceDTO interfacedto : interfaceList) {
						JSONObject connectedDeviceJsonObject = new JSONObject();
						try {
							if (interfacedto.getConnected_devices() != null && interfacedto.getConnected_devices().size() > 0) {
								connectedDeviceJsonObject.put("deviceId", interfacedto.getConnected_devices());
							}
							if (interfacedto.getConnected_mac_addresses() != null && interfacedto.getConnected_mac_addresses().size() > 0) {
								connectedDeviceJsonObject.put("macs", interfacedto.getConnected_mac_addresses());
							}
							interfacedto.setConnected_devices_json(String.valueOf(connectedDeviceJsonObject));

							String id = Generators.timeBasedGenerator().generate().toString();
							interfaceRepository.createInterfaceById(id, interfacedto.getName(), interfacedto.getDownload(),
									interfacedto.getUpload(), interfacedto.getStatus(), interfacedto.getMac_address(),
									interfacedto.getCategory(), interfacedto.getType(), interfacedto.getIfindex(), interfacedto.getSpeed(),
									interfacedto.getConnected_devices_json(), interfacedto.getUser_connected_devices_json(), device_id, interfacedto.getUplink());

						} catch (Exception e) {
							System.out.println("CREATE INTERFACE IS FAILED IN UPDATE INTERFACE");
						}
					}
				}
			}
			try {
				deviceService.updateDeviceInterfaceCount(device_id);
			} catch (Exception e) {
				System.out.println("Error updating device interface count " + e);

			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void deleteByIndex(String ifindex, String id) {
		interfaceRepository.deleteByIndex(ifindex,id);
	}



	
	
	
//	public List<InterfaceDTO> listDeviceSnmpInterfaceByDeviceId(String username, String vdmsid,
//			String dockername, String device_id) {
//		List<InterfaceDTO> allInterfaces = interfaceRepository.listDeviceInterfaceByDeviceId(device_id);
//
//		if(allInterfaces != null && allInterfaces.size() > 0) {
//			for (InterfaceDTO interfaceDTO : allInterfaces) {
//				try {
//					List<DeviceDTO> devices = new ArrayList<DeviceDTO>();
//					List<String> connectedDeviceIdList = null;
//					if(interfaceDTO.getConnected_devices_json() != null) {
//						connectedDeviceIdList = apiCallService.getJSONArrayFromJSONString(interfaceDTO.getConnected_devices_json(),String.class);
//
//						if(connectedDeviceIdList != null && connectedDeviceIdList.size() > 0) {
//							for (String id : connectedDeviceIdList) {
//								DeviceDTO device = deviceService.getDeviceByDeviceId(username, vdmsid, dockername, id);
//								if(device != null) {
//									DeviceDTO formated_device = new DeviceDTO();
//									formated_device.setDisplay_name(device.getDisplay_name());
//									formated_device.setDocker_name(device.getDocker_name());
//									formated_device.setUser_data_name(device.getUser_data_name());
//									formated_device.setId(device.getId()); 
//									formated_device.setIp_address(device.getIp_address());
//									formated_device.setMac_address(device.getMac_address());
//									devices.add(formated_device);
//								}
//							}
//							interfaceDTO.setDevices(devices);
//						}
//					}
//				} catch (Exception e) {
//					System.out.println(e);
//				}
//			}
//			if(allInterfaces.size() > 0) {
//				allInterfaces = this.sortInterfaceListByIfindex(allInterfaces);
//			}
//		}
//		return allInterfaces;
//	}
	
	public List<InterfaceDTO> listDeviceSnmpInterfaceByDeviceId(String username, String vdmsid,
			String dockername, String device_id) {
		List<InterfaceDTO> allInterfaces = interfaceRepository.listDeviceInterfaceByDeviceId(device_id);
		if (allInterfaces != null && allInterfaces.size() > 0) {
			for (InterfaceDTO interfaceDTO : allInterfaces) {
				try {
					List<DeviceDTO> devices = new ArrayList<DeviceDTO>();

					if (interfaceDTO.getConnected_devices_json() != null) {
						JSONObject connectedDeviceIdListObject = new JSONObject(interfaceDTO.getConnected_devices_json()); //converting string to JsonObject

						if (connectedDeviceIdListObject.has("macs")) {
							List<String> connectedMacs = apiCallService.getJSONArrayFromJSONString(connectedDeviceIdListObject.get("macs").toString(), String.class);
							if (connectedMacs != null && connectedMacs.size() > 0) {
								interfaceDTO.setConnected_mac_addresses(connectedMacs);
							}
						}

						if (connectedDeviceIdListObject.has("deviceId")) {
							List<String> connectedDevices = apiCallService.getJSONArrayFromJSONString(connectedDeviceIdListObject.get("deviceId").toString(), String.class);
							if (connectedDevices != null && connectedDevices.size() > 0) {
								for (String connectedDevice : connectedDevices) {
									DeviceDTO device = deviceService.getDeviceByDeviceId(username, vdmsid, dockername, connectedDevice);
									if (device != null) {
										DeviceDTO formated_device = new DeviceDTO();
										formated_device.setDisplay_name(device.getDisplay_name());
										formated_device.setDocker_name(device.getDocker_name());
										formated_device.setUser_data_name(device.getUser_data_name());
										formated_device.setId(device.getId());
										formated_device.setIp_address(device.getIp_address());
										formated_device.setMac_address(device.getMac_address());
										devices.add(formated_device);
									}
								}
								interfaceDTO.setDevices(devices);
							}
						}
					}
				} catch (Exception e) {
					System.out.println(e);
				}
			}
			if (allInterfaces.size() > 0) {
				allInterfaces = this.sortInterfaceListByIfindex(allInterfaces);
			}
		}
		return allInterfaces;
	}
	
	public List<SnmpValuesDTO> listAllNetworkNonNativeSnmpInterfaces(String dockername) {
		try {
			List<SnmpValuesDTO> snmp_data = snmpService.getAllNetworkSnmpDeviceData(dockername);
			List<SnmpValuesDTO> devices = new ArrayList<SnmpValuesDTO>();
			List<String> deviceIdList = new ArrayList<String>();

			for (int i = 0; i < snmp_data.size(); i++) {
				try {
					if (!deviceIdList.contains(snmp_data.get(i).getDevice_id())) {
						deviceIdList.add(snmp_data.get(i).getDevice_id());
						SnmpValuesDTO device = new SnmpValuesDTO();
						device.setId(snmp_data.get(i).getDevice_id());
						device.setMac_address(snmp_data.get(i).getMac_address());

						Map<String, String> connected_device = new HashMap<String, String>();
						connected_device.put(snmp_data.get(i).getOid_key(), snmp_data.get(i).getOid_value());
						device.setConnected_mac_address(connected_device);
						devices.add(device);
					} else {
						int deviceIndex = deviceIdList.indexOf(snmp_data.get(i).getDevice_id());
						devices.get(deviceIndex).getConnected_mac_address().put(snmp_data.get(i).getOid_key(), snmp_data.get(i).getOid_value());
					}
				} catch (Exception e) {
					// TODO: handle exception
				}

			}
			return devices;
		} catch (Exception e) {
			return null;
		}

	}
	
	public void updateUnmanagedSwitch(InterfaceDTO interfaceDTO) {
            interfaceRepository.updateUnmanagedSwitch(interfaceDTO.getIfindex(), interfaceDTO.getDevice_id(), interfaceDTO.getUnmanaged_switch_name(), interfaceDTO.getUnmanaged_switch_type());
    }
	
	//===============================================SNMP TRAP CHANGES==========================================================//
	public void upsertInterface(String dockername, String device_id, InterfaceDTO interfaceDTO) {
		String id = interfaceRepository.getIfId(device_id, interfaceDTO.getIfindex());
		if(id == null)
		{
			this.listDeviceSnmpInterfaceByDeviceIdFromDevice(null, dockername, device_id);
		}
		else if(interfaceDTO.getStatus()!= null)
		{
			interfaceRepository.updateInterfaceStatusByIndex(interfaceDTO.getStatus(), interfaceDTO.getIfindex(),device_id);
		}
		
		try {
			socketService.updateDeviceInterfaceStatus(interfaceDTO, dockername, device_id);
		} catch (Exception e) {
			System.out.println("Error updating socket event of interface status update " + e);
			System.out.println(e);
		}
	}
	//===============================================SNMP TRAP CHANGES==========================================================//
}
