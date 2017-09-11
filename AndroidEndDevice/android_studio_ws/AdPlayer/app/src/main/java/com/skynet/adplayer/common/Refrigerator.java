
package com.skynet.adplayer.common;


import java.util.Date;

public class Refrigerator {

	protected		String              	id                 ;
	protected		String              	title              ;
	protected		String              	refrigeratorManufacturer;
	protected		String              	refrigeratorModel  ;
	protected		String              	refrigeratorSerialNumber;
	protected		double              	refrigeratorCapacity;
	protected		Date                	refrigeratorUpTime ;
	protected		String              	adMachineModel     ;
	protected		String              	adMachineSerialNumber;
	protected		Date                	adMachineUpTime    ;
	protected		String              	rfidReaderId       ;
	protected		double              	longitude          ;
	protected		double              	latitude           ;
	protected		Date                	lastReportTime     ;
	protected		String              	apkVersion         ;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getRefrigeratorManufacturer() {
		return refrigeratorManufacturer;
	}

	public void setRefrigeratorManufacturer(String refrigeratorManufacturer) {
		this.refrigeratorManufacturer = refrigeratorManufacturer;
	}

	public String getRefrigeratorModel() {
		return refrigeratorModel;
	}

	public void setRefrigeratorModel(String refrigeratorModel) {
		this.refrigeratorModel = refrigeratorModel;
	}

	public String getRefrigeratorSerialNumber() {
		return refrigeratorSerialNumber;
	}

	public void setRefrigeratorSerialNumber(String refrigeratorSerialNumber) {
		this.refrigeratorSerialNumber = refrigeratorSerialNumber;
	}

	public double getRefrigeratorCapacity() {
		return refrigeratorCapacity;
	}

	public void setRefrigeratorCapacity(double refrigeratorCapacity) {
		this.refrigeratorCapacity = refrigeratorCapacity;
	}

	public Date getRefrigeratorUpTime() {
		return refrigeratorUpTime;
	}

	public void setRefrigeratorUpTime(Date refrigeratorUpTime) {
		this.refrigeratorUpTime = refrigeratorUpTime;
	}

	public String getAdMachineModel() {
		return adMachineModel;
	}

	public void setAdMachineModel(String adMachineModel) {
		this.adMachineModel = adMachineModel;
	}

	public String getAdMachineSerialNumber() {
		return adMachineSerialNumber;
	}

	public void setAdMachineSerialNumber(String adMachineSerialNumber) {
		this.adMachineSerialNumber = adMachineSerialNumber;
	}

	public Date getAdMachineUpTime() {
		return adMachineUpTime;
	}

	public void setAdMachineUpTime(Date adMachineUpTime) {
		this.adMachineUpTime = adMachineUpTime;
	}

	public String getRfidReaderId() {
		return rfidReaderId;
	}

	public void setRfidReaderId(String rfidReaderId) {
		this.rfidReaderId = rfidReaderId;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public Date getLastReportTime() {
		return lastReportTime;
	}

	public void setLastReportTime(Date lastReportTime) {
		this.lastReportTime = lastReportTime;
	}

	public String getApkVersion() {
		return apkVersion;
	}

	public void setApkVersion(String apkVersion) {
		this.apkVersion = apkVersion;
	}
}

