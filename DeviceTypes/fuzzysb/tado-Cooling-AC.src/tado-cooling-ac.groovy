/**
 *  Copyright 2015 Stuart Buchanan
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	Tado Thermostat
 *
 *	Author: Stuart Buchanan, Based on original work by Ian M with thanks
 *  Date: 2016-01-23 v1.1 fixed error in Tado Mode detection
 *	Date: 2016-01-22 v1.1 Add Heating & Cooling Controls (initial offering, will need to look into adding all possible commands)
 *	Date: 2015-12-04 v1.0 Initial Release With Temperatures & Relative Humidity
 */
 
import groovy.json.JsonOutput

preferences {
	input("username", "text", title: "Username", description: "Your Tado username")
	input("password", "password", title: "Password", description: "Your Tado password")
}  
 
metadata {
	definition (name: "Tado AC Thermostat", namespace: "fuzzysb", author: "Stuart Buchanan") {
		capability "Actuator"
        capability "Temperature Measurement"
		capability "Thermostat"
		capability "Presence Sensor"
        capability "Relative Humidity Measurement"
		capability "Polling"
		capability "Refresh"
        
        command "heatingSetpointUp"
        command "heatingSetpointDown"
        command "coolingSetpointUp"
        command "coolingSetpointDown"
        command "dry"
        command "on"
        
	}

	// simulator metadata
	simulator {
		// status messages

		// reply messages
	}

	tiles(scale: 2){
      	multiAttributeTile(name: "thermostat", type:"thermostat", width:6, height:4) {
			tileAttribute("device.temperature", key:"PRIMARY_CONTROL", canChangeIcon: true, canChangeBackground: true){
            	attributeState "default", label:'${currentValue}° C', unit:"C", backgroundColor:"#fab907", icon:"st.Home.home1"
            }
			tileAttribute ("device.Humidity", key: "SECONDARY_CONTROL") {
				attributeState "default", label: '${currentValue}% Humidity', icon:"st.Weather.weather12"
			}
		}
        
        standardTile("autoOperation", "device.autoOperation", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {         
			state("SLEEP", label:'${name}', backgroundColor:"#0164a8", icon:"st.Bedroom.bedroom2")
            state("HOME", label:'${name}', backgroundColor:"#fab907", icon:"st.Home.home2")
            state("AWAY", label:'${name}', backgroundColor:"#62aa12", icon:"st.Outdoor.outdoor18")
            state("OFF", label:'${name}', backgroundColor:"#ffffff", icon:"st.switches.switch.off", defaultState: true)
            state("MANUAL", label:'${name}', backgroundColor:"#ffffff", icon:"st.Weather.weather1")
		}
    	
        standardTile("refresh", "device.switch", inactiveLabel: false, width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        standardTile("ACMode", "device.ACMode", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
        	state("HEAT", label:'${name}', backgroundColor:"#ea2a2a", icon:"st.Weather.weather14")
            state("COOL", label:'${name}', backgroundColor:"#089afb", icon:"st.Weather.weather7")
            state("DRY", label:'${name}', backgroundColor:"#ab7e13", icon:"st.Weather.weather12")
            state("FAN", label:'${name}', backgroundColor:"#ffffff", icon:"st.Appliances.appliances11")
            state("AUTO", label:'${name}', backgroundColor:"#62aa12", icon:"st.Electronics.electronics13")
            state("OFF", label:'${name}', backgroundColor:"#ffffff", icon:"st.switches.switch.off", defaultState: true)  
		}
        
		valueTile("setPointTemp", "device.setPointTemp", width: 2, height: 1, decoration: "flat") {
			state "default", label: 'Set Point\r\n\${currentValue}° C'
		}
		
		valueTile("outsidetemperature", "device.outsidetemperature", width: 2, height: 1, decoration: "flat") {
			state "outsidetemperature", label: 'Outside Temp\r\n${currentValue}° C'
		}
       
		standardTile("ACFanSpeed", "device.ACFanSpeed", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
        	state("OFF", label:'${name}', backgroundColor:"#ffffff", icon:"st.Appliances.appliances11", defaultState: true)
            state("AUTO", label:'${name}', backgroundColor:"#ffffff", icon:"st.Appliances.appliances11")
            state("HIGH", label:'${name}', backgroundColor:"#ffffff", icon:"st.Appliances.appliances11")
            state("MIDDLE", label:'${name}', backgroundColor:"#ffffff", icon:"st.Appliances.appliances11")
            state("LOW", label:'${name}', backgroundColor:"#ffffff", icon:"st.Appliances.appliances11")       
		}
		standardTile("setAuto", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"Auto", action:"thermostat.auto"
		}
        standardTile("setDry", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"Dry", action:"dry"
		}
        standardTile("setOn", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"On", action:"on"
		}
        standardTile("setOff", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"Off", action:"thermostat.off"
		}
        standardTile("coolingSetpointUp", "device.coolingSetpoint", canChangeIcon: false, decoration: "flat") {
            state "coolingSetpointUp", label:'  ', action:"coolingSetpointUp", icon:"st.thermostat.thermostat-up", backgroundColor:"#0683ff"
        }
		standardTile("coolingSetpointDown", "device.coolingSetpoint", canChangeIcon: false, decoration: "flat") {
            state "coolingSetpointDown", label:'  ', action:"coolingSetpointDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#0683ff"
        }
		standardTile("heatingSetpointUp", "device.heatingSetpoint", canChangeIcon: false, decoration: "flat") {
            state "heatingSetpointUp", label:'  ', action:"heatingSetpointUp", icon:"st.thermostat.thermostat-up", backgroundColor:"#bc2323"
        }
        standardTile("heatingSetpointDown", "device.heatingSetpoint", canChangeIcon: false, decoration: "flat") {
            state "heatingSetpointDown", label:'  ', action:"heatingSetpointDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#bc2323"
        }
		
		main(["thermostat"])
		details(["thermostat","ACMode","coolingSetpointUp","coolingSetpointDown","autoOperation","heatingSetpointUp","heatingSetpointDown","outsidetemperature","setPointTemp","ACFanSpeed","refresh","setAuto","setDry","setOn","setOff"])
	}
}

// Parse incoming device messages to generate events
private parseResponse(resp) {
    log.debug("Executing parseResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
	def temperatureUnit = "C"
	def humidityUnit = "%"
    if(resp.status == 200) {
    	log.debug("Executing parseResponse.successTrue")
        
        def temperature = Math.round(resp.data.insideTemp)
        log.debug("Read temperature: " + temperature)
        sendEvent(name: 'temperature', value: temperature, unit: temperatureUnit)
        log.debug("Send Temperature Event Fired")
		
		state.homeId = resp.data.homeId
        log.debug("Got HomeID Value: " + state.homeId)

        def autoOperation = resp.data.autoOperation
        if(resp.data.operation == "NO_FREEZE"){
        	autoOperation = "OFF"
        }else if(resp.data.operation == "MANUAL"){
        	autoOperation = "MANUAL"
        }
        log.debug("Read autoOperation: " + autoOperation)
        sendEvent(name: 'autoOperation', value: autoOperation)
        log.debug("Send autoOperation Event Fired")
        
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}

private parseputResponse(resp) {
	log.debug("Executing parseHvacResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
}

private parseHvacResponse(resp) {    
    log.debug("Executing parseHvacResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
	def temperatureUnit = "C"
	def humidityUnit = "%"
    if(resp.status == 200) {
    	log.debug("Executing parseHvacResponse.successTrue")
        
        def humidity 
        if (resp.data.humidity.percentage != null){
        	humidity = resp.data.humidity.percentage
        }else{
        	humidity = "--"
        }
        log.debug("Read humidity: " + humidity)
			       
        sendEvent(name: 'humidity', value: humidity,unit: humidityUnit)
	}	
	def ACMode
    def ACFanSpeed
    def setPointTemp
    if (resp.data.acSetting.power == "OFF"){
       	ACMode = "OFF"
        log.debug("Read ACMode: " + ACMode)
		ACFanSpeed = "OFF"
        log.debug("Read acFanSpeed: " + ACFanSpeed)
		setPointTemp = "0"
        log.debug("Read setPointTemp: " + setPointTemp)
    }
    else if (resp.data.acSetting.power == "ON"){
       	ACMode = resp.data.acSetting.mode
		log.debug("Read ACMode: " + ACMode)
		ACFanSpeed = resp.data.acSetting.fanSpeed
        log.debug("Read acFanSpeed: " + ACFanSpeed)
        if (ACMode == "DRY"){
        	setPointTemp = "--"
        }else if (ACMode == "AUTO"){
                	setPointTemp = "--"
        }else{
        	setPointTemp = Math.round(resp.data.acSetting.temperature.celsius)
        }
        log.debug("Read setPointTemp: " + setPointTemp)
    }
	else{
        log.debug("Executing parseHvacResponse.successFalse")
    }
    sendEvent(name: 'ACFanSpeed', value: ACFanSpeed)
    log.debug("Send ACFanSpeed Event Fired")
	sendEvent(name: 'ACMode', value: ACMode)
    log.debug("Send ACMode Event Fired")
    sendEvent(name: 'setPointTemp', value: setPointTemp, unit: temperatureUnit)
    log.debug("Send setPointTemp Event Fired")
	

}

private parseweatherResponse(resp) {
    log.debug("Executing parseweatherResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
	def temperatureUnit = "C"
    if(resp.status == 200) {
    	log.debug("Executing parseResponse.successTrue")
        
        def outsidetemperature = Math.round(resp.data.outsideTemperature.celsius)
        log.debug("Read outside temperature: " + outsidetemperature)
        sendEvent(name: 'outsidetemperature', value: outsidetemperature , unit: temperatureUnit)
        log.debug("Send Outside Temperature Event Fired")
        
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}

def poll() {
	log.debug "Executing 'poll'"
	refresh()
}

def refresh() {
	log.debug "Executing 'refresh'"
    statusCommand()
    hvacStatusCommand()
    weatherStatusCommand()
    
}

def auto() {
	log.debug "Executing 'auto'"
	autoCommand()
    refresh()
}

def on() {
	log.debug "Executing 'on'"
	onCommand()
    refresh()
}

def off() {
	log.debug "Executing 'off'"
	offCommand()
    refresh()
}

def dry() {
	log.debug "Executing 'dry'"
	dryCommand()
    refresh()
}

def setHeatingSetpoint(targetTemperature) {
	log.debug "Executing 'setHeatingSetpoint'"
    log.debug "Target Temperature ${targetTemperature}"
    setHeatingTempCommand(targetTemperature)
	refresh()
}

def heatingSetpointUp(){
	log.debug "Current SetPoint Is " + (device.currentValue("setPointTemp")).toString()
	int newSetpoint = (device.currentValue("setPointTemp")).toInteger() + 1
	log.debug "Setting heatingSetpoint up to: ${newSetpoint}"
	setHeatingSetpoint(newSetpoint)
}

def heatingSetpointDown(){
	log.debug "Current SetPoint Is " + (device.currentValue("setPointTemp")).toString()
	int newSetpoint = (device.currentValue("setPointTemp")).toInteger() - 1
	log.debug "Setting heatingSetpoint down to: ${newSetpoint}"
	setHeatingSetpoint(newSetpoint)
}

def setCoolingSetpoint(targetTemperature) {
	log.debug "Executing 'setCoolingSetpoint'"
    log.debug "Target Temperature ${targetTemperature}"
    setCoolingTempCommand(targetTemperature)
	refresh()
}

def coolingSetpointUp(){
	log.debug "Current SetPoint Is " + (device.currentValue("setPointTemp")).toString()
	int newSetpoint = (device.currentValue("setPointTemp")).toInteger() + 1
	log.debug "Setting coolingSetpoint up to: ${newSetpoint}"
	setHeatingSetpoint(newSetpoint)
}

def coolingSetpointDown(){
	log.debug "Current SetPoint Is " + (device.currentValue("setPointTemp")).toString()
	int newSetpoint = (device.currentValue("setPointTemp")).toInteger() - 1
	log.debug "Setting coolingSetpoint down to: ${newSetpoint}"
	setHeatingSetpoint(newSetpoint)
}


private sendCommand(method, args = []) {
    def methods = [
		'status': [
        			uri: "https://my.tado.com", 
                    path: "/mobile/1.6/getCurrentState", 
                    requestContentType: "application/json", 
                    query: [username:settings.username, password:settings.password]
                    ],
        'hvacStatus': [
        			uri: "https://my.tado.com", 
                    path: "/api/v1/home/" + state.homeId + "/hvacState", 
                    requestContentType: "application/json", 
                    query: [username:settings.username, password:settings.password]
                    ],
		'temperature': [	
        			uri: "https://my.tado.com",
        			path: "/api/v2/homes/" + state.homeId + "/zones/1/overlay",
        			requestContentType: "application/json",
                    query: [username:settings.username, password:settings.password],
                  	body: args[0]
                   	],
		'weatherStatus': [	
        			uri: "https://my.tado.com",
        			path: "/api/v1/home/" + state.homeId + "/weather",
        			requestContentType: "application/json",
    				query: [username:settings.username, password:settings.password]
                   	]
	]

	def request = methods.getAt(method)
    
    log.debug "Http Params ("+request+")"
    
    try{
        log.debug "Executing 'sendCommand'"
        
        if (method == "status"){
            httpGet(request) { resp ->            
                parseResponse(resp)
            }
        }else if (method == "hvacStatus"){
            httpGet(request) { resp ->            
                parseHvacResponse(resp)
            }
		}else if (method == "temperature"){
            httpPut(request) { resp ->            
                parseputResponse(resp)
            }
        }else if (method == "weatherStatus"){
            log.debug "calling weatherStatus Method"
            httpGet(request) { resp ->            
                parseweatherResponse(resp)
            }
        }else{
            httpGet(request)
        }
    } catch(Exception e){
        debug("___exception: " + e)
    }
}



// Commands to device
def statusCommand(){
	log.debug "Executing 'sendCommand.statusCommand'"
	sendCommand("status",[])
}

def autoCommand(){
	log.debug "Executing 'sendCommand.autoCommand'"
	def jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"AUTO", power:"ON", type:"AIR_CONDITIONING"], termination:[type:"TADO_MODE"]])
	sendCommand("temperature",[jsonbody])
}

def dryCommand(){
	log.debug "Executing 'sendCommand.dryCommand'"
    def jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"DRY", power:"ON", type:"AIR_CONDITIONING"], termination:[type:"TADO_MODE"]])
	sendCommand("temperature",[jsonbody])
}

def setCoolingTempCommand(targetTemperature){
	log.debug "Executing 'sendCommand.setCoolingTempCommand' to ${targetTemperature}"
    def jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:"HIGH", mode:"COOL", power:"ON", temperature:[celsius:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:"TADO_MODE"]])
	sendCommand("temperature",[jsonbody])
}

def setHeatingTempCommand(targetTemperature){
	log.debug "Executing 'sendCommand.setHeatingTempCommand' to ${targetTemperature}"
    def jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:"AUTO", mode:"HEAT", power:"ON", temperature:[celsius:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:"TADO_MODE"]])
	sendCommand("temperature",[jsonbody])
}

def offCommand(){
	log.debug "Executing 'sendCommand.offCommand'"
    def jsonbody = new groovy.json.JsonOutput().toJson([setting:[type:"AIR_CONDITIONING", power:"OFF"], termination:[type:"TADO_MODE"]])
	sendCommand("temperature",[jsonbody])
}

def onCommand(){
	log.debug "Executing 'sendCommand.onCommand'"
    def jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:"AUTO", mode:"COOL", power:"ON", temperature:[celsius:21], type:"AIR_CONDITIONING"], termination:[type:"TADO_MODE"]])
	sendCommand("temperature",[jsonbody])
}

def hvacStatusCommand(){
	log.debug "Executing 'sendCommand.hvacStatusCommand'"
	sendCommand("hvacStatus",[])
}

def weatherStatusCommand(){
	log.debug "Executing 'sendCommand.weatherStatusCommand'"
	sendCommand("weatherStatus",[])
}