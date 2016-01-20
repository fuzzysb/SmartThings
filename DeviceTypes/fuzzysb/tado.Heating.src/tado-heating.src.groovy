/**
 *  Copyright 2015 SmartThings
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
 *	Author: Ian M
 *
 *	Updates: 
 *  2016-01-20  Updated hvacStatus to include include the correct HomeId for Humidity Value
 *  2016-01-15  Refactored API request code and added querying/display of humidity
 *	2015-12-23	Added functionality to change thermostat settings
 *	2015-12-04	Initial release
 */
 
preferences {
	input("username", "text", title: "Username", description: "Your Tado username")
	input("password", "password", title: "Password", description: "Your Tado password")
}  
 
metadata {
	definition (name: "Tado Thermostat", namespace: "ianm", author: "Ian M") {
		capability "Actuator"
        capability "Temperature Measurement"
		capability "Thermostat"
        capability "Presence Sensor"
		capability "Polling"
		capability "Refresh"
        capability "Relative Humidity Measurement"
        
        command "heatingSetpointUp"
        command "heatingSetpointDown"
        command "auto"
	}

	// simulator metadata
	simulator {
		// status messages

		// reply messages
	}

	tiles(scale: 2){
      	multiAttributeTile(name: "thermostat", width: 6, height: 4, type:"lighting") {
			tileAttribute("device.temperature", key:"PRIMARY_CONTROL", canChangeIcon: true){
            	attributeState "default", label:'${currentValue}° C', unit:"C", backgroundColor:"#fab907", icon:"st.Home.home1"
            }
            tileAttribute ("thermostatOperatingState", key: "SECONDARY_CONTROL") {
				attributeState "thermostatOperatingState", label:'${currentValue}'
			}
		}
        
        valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 1, decoration: "flat") {
			state("default", label: '${currentValue}° C')
		}

        standardTile("thermostatMode", "device.thermostatMode", width: 2, height: 2) {
            state "HOME", label:'${name}', backgroundColor:"#fab907", icon:"st.Home.home2"
            state "AWAY", label:'${name}', backgroundColor:"#62aa12", icon:"st.Outdoor.outdoor18"
            state "SLEEP", label:'${name}', backgroundColor:"#0164a8", icon:"st.Bedroom.bedroom2"
            state "OFF", label:'${name}', backgroundColor:"#ffffff", icon:"st.switches.switch.off"
            state "MANUAL", label:'${name}', backgroundColor:"#ffffff", icon:"st.Weather.weather1"
		}
        
        standardTile("presence", "device.presence", width: 2, height: 2) {
			state "present", labelIcon:"st.presence.tile.present", backgroundColor:"#53a7c0"
			state "not present", labelIcon:"st.presence.tile.not-present", backgroundColor:"#ebeef2"
		}
      	
        standardTile("refresh", "device.switch", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}		
        
        valueTile("humidity", "device.humidity", width: 2, height: 1, decoration: "flat") {
            state "default", label:'Humidity: ${currentValue}%'
        }
        
        standardTile("setAuto", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"Auto", action:"thermostat.auto"
		}

        standardTile("setManual", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"Manual", action:"thermostat.heat"
		}

        standardTile("setOff", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"Off", action:"thermostat.off"
		}
        
        standardTile("heatingSetpointUp", "device.heatingSetpoint", canChangeIcon: false, decoration: "flat") {
            state "heatingSetpointUp", label:'  ', action:"heatingSetpointUp", icon:"st.thermostat.thermostat-up"
        }

        standardTile("heatingSetpointDown", "device.heatingSetpoint", canChangeIcon: false, decoration: "flat") {
            state "heatingSetpointDown", label:'  ', action:"heatingSetpointDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#bc2323"
        }

        
        standardTile("thermostatFanMode", "device.thermostatFanMode", decoration: "flat") {
            state "HOME", label:'${name}', backgroundColor:"#fab907", icon:"st.Home.home2"
            state "AWAY", label:'${name}', backgroundColor:"#62aa12", icon:"st.Outdoor.outdoor18"
            state "SLEEP", label:'${name}', backgroundColor:"#0164a8", icon:"st.Bedroom.bedroom2"
            state "OFF", label:'${name}', backgroundColor:"#ffffff", icon:"st.switches.switch.off"
            state "MANUAL", label:'${name}', backgroundColor:"#ffffff", icon:"st.Weather.weather1"
        }

		main "thermostat"
		details (["thermostat","humidity","heatingSetpoint","thermostatMode","refresh","heatingSetpointUp","heatingSetpointDown","setAuto","setManual","setOff"])
	}
}

// Parse incoming device messages to generate events
private parseResponse(resp) {
    log.debug("Executing parseResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
    	log.debug("Executing parseResponse.successTrue")
        
        def temperature = Math.round(resp.data.insideTemp)
        log.debug("Read temperature: " + temperature)
        
		state.homeId = resp.data.homeId
        log.debug("Got HomeID Value: " + state.homeId)
		
        def controlPhase = resp.data.controlPhase
        if(resp.data.controlPhase == "UNDEFINED"){
        	controlPhase = "FROST PROTECTION"
        }
        log.debug("Read controlPhase: " + controlPhase)
        
        def setPointTemp 
        if (resp.data.setPointTemp != null){
        	setPointTemp = Math.round(resp.data.setPointTemp)
        }else{
        	setPointTemp = "--"
        }
        log.debug("Read setPointTemp: " + setPointTemp)
        
        def autoOperation = resp.data.autoOperation
        if(resp.data.operation == "NO_FREEZE"){
        	autoOperation = "OFF"
        }else if(resp.data.operation == "MANUAL"){
        	autoOperation = "MANUAL"
        }
        log.debug("Read autoOperation: " + autoOperation)
        
        def presence
        if(resp.data.operation == "AWAY"){
        	presence = "not present"
        }else {
        	presence = "present"
        }
        log.debug("Read presence: " + presence)
        
        def temperatureUnit = "C"
        
        sendEvent(name: 'temperature', value: temperature, unit: temperatureUnit)
        sendEvent(name: 'heatingSetpoint', value: setPointTemp, unit: temperatureUnit)
		sendEvent(name: 'thermostatOperatingState', value: controlPhase)
        sendEvent(name: 'thermostatMode', value: autoOperation)
        sendEvent(name: 'thermostatFanMode', value: autoOperation)  
        sendEvent(name: 'presence', value: presence)
    }else{
        log.debug("Executing parseResponse.successFalse")
    }
}

private parseHvacResponse(resp) {
    log.debug("Executing parseHvacResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
    	log.debug("Executing parseHvacResponse.successTrue")
        
        def humidity 
        if (resp.data.humidity.percentage != null){
        	humidity = resp.data.humidity.percentage
        }else{
        	humidity = "--"
        }
        log.debug("Read humidity: " + humidity)
        
        sendEvent(name: 'humidity', value: humidity)
    }else{
        log.debug("Executing parseHvacResponse.successFalse")
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
}

def auto() {
	log.debug "Executing 'auto'"
	autoCommand()
    refresh()
}

def off() {
	log.debug "Executing 'off'"
	offCommand()
    refresh()
}

def heat() {
	log.debug "Executing 'heat'"
	manualCommand()
    refresh()
}

def setHeatingSetpoint(targetTemperature) {
	log.debug "Executing 'setHeatingSetpoint'"
    log.debug "Target Temperature ${targetTemperature}"
    setTempCommand(targetTemperature)
	refresh()
}

def heatingSetpointUp(){
	int newSetpoint = device.currentValue("heatingSetpoint") + 1
	log.debug "Setting heatingSetpoint up to: ${newSetpoint}"
	setHeatingSetpoint(newSetpoint)
}

def heatingSetpointDown(){
	int newSetpoint = device.currentValue("heatingSetpoint") - 1
	log.debug "Setting heatingSetpoint down to: ${newSetpoint}"
	setHeatingSetpoint(newSetpoint)
}

private sendCommand(method, args = []) {
    def methods = [
		'status': [
        			uri: "https://my.tado.com", 
                    path: "/mobile/1.9/getCurrentState", 
                    requestContentType: "application/json", 
                    query: [username:settings.username, password:settings.password]
                    ],
        'hvacStatus': [
        			uri: "https://my.tado.com", 
                    path: "/api/v1/home/" + state.homeId + "/hvacState", 
                    requestContentType: "application/json", 
                    query: [username:settings.username, password:settings.password]
                    ],
		'thermostat_mode': [	
        			uri: "https://my.tado.com",
        			path: "/mobile/1.9/updateThermostatSettings",
        			requestContentType: "application/json",
    				query: [username:settings.username, password:settings.password, setMode:args[0]]
                   	],
		'temperature': [	
        			uri: "https://my.tado.com",
        			path: "/mobile/1.9/updateThermostatSettings",
        			requestContentType: "application/json",
    				query: [username:settings.username, password:settings.password, setMode:args[0], manualTemp:args[1]]
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
	sendCommand("thermostat_mode",["AUTO"])
}

def manualCommand(){
	log.debug "Executing 'sendCommand.manualCommand'"
	sendCommand("thermostat_mode",["MANUAL"])
}

def setTempCommand(targetTemperature){
	log.debug "Executing 'sendCommand.setTempCommand' to ${targetTemperature}"
	sendCommand("temperature",["MANUAL",targetTemperature])
}

def offCommand(){
	log.debug "Executing 'sendCommand.offCommand'"
	sendCommand("thermostat_mode",["NO_FREEZE"])
}

def hvacStatusCommand(){
	log.debug "Executing 'sendCommand.hvacStatusCommand'"
	sendCommand("hvacStatus",[])
}