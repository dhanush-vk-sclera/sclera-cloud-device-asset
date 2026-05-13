package io.sclera.models;

import java.math.BigInteger;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;

import io.sclera.dto.DockerInfoDto;
import io.sclera.dto.VlanDTO;


@SqlResultSetMapping(name = "systeminterfacelistmapping",
		classes = {
				@ConstructorResult(
						targetClass = DockerInfoDto.class,
						columns = {
								@ColumnResult(name = "interface_out", type = String.class),
								@ColumnResult(name = "interface_status", type = String.class)
						}
				)
		}
)

@NamedNativeQuery (
		name = "System_interface.getInterfaceStatusList",
		query = "SELECT interface_name as interface_out, status as interface_status FROM system_interface ",
		resultSetMapping = "systeminterfacelistmapping"
)


@SqlResultSetMapping(name = "systeminterfacepidmapping",
		classes = {
				@ConstructorResult(
						targetClass = VlanDTO.class,
						columns = {
								@ColumnResult(name = "pid", type = String.class),
								@ColumnResult(name = "timestamp", type = BigInteger.class)

						}
				)
		}
)

@NamedNativeQuery (
		name = "System_interface.getVlanDiscoverPidByInterfaceName",
		query = "SELECT pid, timestamp FROM system_interface WHERE interface_name = ?1",
		resultSetMapping = "systeminterfacepidmapping"
)



@Entity
public class System_interface {
	
	@Id
	private String interface_name;
	private String status;
	private String pid;
	private BigInteger timestamp;
	
	
	public String getInterface_name() {
		return interface_name;
	}
	public void setInterface_name(String interface_name) {
		this.interface_name = interface_name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public BigInteger getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(BigInteger timestamp) {
		this.timestamp = timestamp;
	}
	
	
	
	

}
