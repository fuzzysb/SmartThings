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
 * 	Author: Stuart Buchanan, Based on original work by Ian M with thanks. also source for icons was from @tonesto7's excellent Nest Manager.
 * 	Date: 2016-04-25 v1.4 Tado Hot water does not actually return the current water temps, it only returns the Current set point temp. to get around this when the power is on for the hot water botht the temp and setpoint will both display the setpoint value, otherwise will display --
 * 	Date: 2016-04-25 v1.3 Finally found time to update this with the lessons learnt from the Tado Cooling Device Type. will bring better support for RM and Thermostat Director
 * 	Date: 2016-04-08 v1.2 added setThermostatMode(mode) function to work better with Rule Machine and Thermostat Mode Director
 *	Date: 2016-04-05 v1.1 change of default Water Heating Temps can now be defined in device preferences (default Value is 90C). 
 *	Date: 2016-04-05 v1.0 Initial release
 */
 
preferences {
	input("username", "text", title: "Username", description: "Your Tado username")
	input("password", "password", title: "Password", description: "Your Tado password")
    input("manualmode", "enum", title: "Default Manual Overide Method", options: ["TADO_MODE","MANUAL"], required: false, defaultValue:"TADO_MODE")
	input("defWaterTemp", "number", title: "Default Water Heating Temperature", required: false, defaultValue: 90)
}  
 
metadata {
	definition (name: "Tado Hot Water Control", namespace: "fuzzysb", author: "Stuart Buchanan") {
		capability "Actuator"
        capability "Temperature Measurement"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Setpoint"
		capability "Thermostat Mode"
		capability "Thermostat Operating State"
		capability "Thermostat"
		capability "Polling"
		capability "Refresh"
        
        attribute "tadoMode", "string"
		command "temperatureUp"
        command "temperatureDown"
        command "heatingSetpointUp"
        command "heatingSetpointDown"
		command "on"
        command "endManualControl"
		command "emergencyHeat"

	}

	// simulator metadata
	simulator {
		// status messages

		// reply messages
	}

tiles(scale: 2){
      	multiAttributeTile(name: "thermostat", type:"thermostat", width:6, height:4) {
			tileAttribute("device.temperature", key:"PRIMARY_CONTROL", canChangeIcon: true, canChangeBackground: true){
            	attributeState "default", label:'${currentValue}°', backgroundColor:"#fab907", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/tap_icon.png"
            }
			tileAttribute("device.temperature", key: "VALUE_CONTROL") {
    			attributeState("VALUE_UP", action: "temperatureUp")
    			attributeState("VALUE_DOWN", action: "temperatureDown")
  			}
            tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
    			attributeState("idle", backgroundColor:"#666666")
    			attributeState("heating", backgroundColor:"#ff471a")
                attributeState("emergency heat", backgroundColor:"#ff471a")
  			}
			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
    			attributeState("off", label:'${name}')
    			attributeState("heat", label:'${name}')
  			}
			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
    			attributeState("default", label:'${currentValue}', unit:"dF")
  			}
		}
		
        valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 1, decoration: "flat") {
			state "default", label: 'Set Point\r\n\${currentValue}°'
		}

        standardTile("tadoMode", "device.tadoMode", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {         
			state("SLEEP", label:'${name}', backgroundColor:"#0164a8", icon:"st.Bedroom.bedroom2")
            state("HOME", label:'${name}', backgroundColor:"#fab907", icon:"st.Home.home2")
            state("AWAY", label:'${name}', backgroundColor:"#62aa12", icon:"st.Outdoor.outdoor18")
            state("OFF", label:'', backgroundColor:"#ffffff", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/hvac_off.png", defaultState: true)
            state("MANUAL", label:'${name}', backgroundColor:"#804000", icon:"st.Weather.weather1")
		}
    	
		standardTile("thermostatMode", "device.thermostatMode", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
        	state("heat", label:'HEAT', backgroundColor:"#ea2a2a", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/heat_mode_icon.png")
            state("off", label:'', backgroundColor:"#ffffff", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/hvac_off.png", defaultState: true)  
		}
		
        standardTile("refresh", "device.switch", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}		
        standardTile("Off", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"thermostat.off", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/hvac_off.png"
		}
		standardTile("emergencyHeat", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"thermostat.emergencyHeat", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/emergencyHeat.png"
		}
		valueTile("outsidetemperature", "device.outsidetemperature", width: 2, height: 1, decoration: "flat") {
			state "outsidetemperature", label: 'Outside Temp\r\n${currentValue}°'
		}
		standardTile("heat", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"thermostat.heat", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/hvac_heat.png"
		}
		standardTile("heatingSetpointUp", "device.heatingSetpoint", width: 1, height: 1, canChangeIcon: false, decoration: "flat") {
            state "heatingSetpointUp", label:'', action:"heatingSetpointUp", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/heat_arrow_up.png"
        }
        standardTile("heatingSetpointDown", "device.heatingSetpoint", width: 1, height: 1, canChangeIcon: false, decoration: "flat") {
            state "heatingSetpointDown", label:'', action:"heatingSetpointDown", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/heat_arrow_down.png"
        }
		standardTile("endManualControl", "device.thermostat", width: 2, height: 1, canChangeIcon: false, canChangeBackground: true, decoration: "flat") {
            state("default", label:'', action:"endManualControl", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado.Hot.Water.src/Images/endManual.png")
		}
		main "thermostat"
		details (["thermostat","thermostatMode","outsidetemperature","heatingSetpoint","refresh","heatingSetpointUp","heatingSetpointDown","tadoMode","emergencyHeat","heat","Off","endManualControl"])
	}
}
def updated(){
	def cmds = [
	getidCommand(),
	getTempUnitCommand(),
	getCapabilitiesCommand(),
	]
	delayBetween(cmds, 2000)
}

def installed(){
	def cmds = [
	getidCommand(),
	getTempUnitCommand(),
	getCapabilitiesCommand(),
	refresh()
	]
	delayBetween(cmds, 2000)
}

def poll() {
	log.debug "Executing 'poll'"
	refresh()
}

def refresh() {
	log.debug "Executing 'refresh'"
    statusCommand()
    weatherStatusCommand()
}

def auto() {
	log.debug "Executing 'auto'"
	autoCommand()
    statusCommand()
}

def on() {
	log.debug "Executing 'on'"
	onCommand()
    statusCommand()
}

def off() {
	log.debug "Executing 'off'"
	offCommand()
    statusCommand()
}

def setHeatingSetpoint(targetTemperature) {
	log.debug "Executing 'setHeatingSetpoint'"
    log.debug "Target Temperature ${targetTemperature}"
    setHeatingTempCommand(targetTemperature)
	statusCommand()
}

def temperatureUp(){
	if (device.currentValue("thermostatMode") == "heat") {
    	heatingSetpointUp()
    } else {
    	log.debug ("temperature setpoint not supported in the current thermostat mode")
    }
}

def temperatureDown(){
	if (device.currentValue("thermostatMode") == "heat") {
    	heatingSetpointDown()
    } else {
    	log.debug ("temperature setpoint not supported in the current thermostat mode")
    }
}

def heatingSetpointUp(){
	log.debug "Current SetPoint Is " + (device.currentValue("thermostatSetpoint")).toString()
	if(state.supportsWaterTempControl == "true"){ 
		if ((device.currentValue("thermostatSetpoint").toInteger() - 1 ) < state.MinHeatTemp){
			log.debug("cannot decrease heat setpoint, its already at the minimum level of " + state.MinHeatTemp)
		} else {
			int newSetpoint = (device.currentValue("thermostatSetpoint")).toInteger() + 1
			log.debug "Setting heatingSetpoint up to: ${newSetpoint}"
			setHeatingSetpoint(newSetpoint)
			statusCommand()
		}
	} else {
		log.debug "Hot Water Temperature Capability Not Supported"
	}
}

def heatingSetpointDown(){
	log.debug "Current SetPoint Is " + (device.currentValue("thermostatSetpoint")).toString()
	if(state.supportsWaterTempControl == "true"){ 
		if ((device.currentValue("thermostatSetpoint").toInteger() + 1 ) > state.MaxHeatTemp){
			log.debug("cannot increase heat setpoint, its already at the maximum level of " + state.MaxHeatTemp)
		} else {
			int newSetpoint = (device.currentValue("thermostatSetpoint")).toInteger() - 1
			log.debug "Setting heatingSetpoint down to: ${newSetpoint}"
			setHeatingSetpoint(newSetpoint)
			statusCommand()
		}
	} else {
		log.debug "Hot Water Temperature Capability Not Supported"
	}
}

// Parse incoming device messages to generate events
private parseMeResponse(resp) {
    log.debug("Executing parseMeResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
    	log.debug("Executing parseMeResponse.successTrue")
        state.homeId = resp.data.homes[0].id
        log.debug("Got HomeID Value: " + state.homeId)
        
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}

private parseputResponse(resp) {
	log.debug("Executing parseputResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
}

private parsedeleteResponse(resp) {
	log.debug("Executing parsedeleteResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
}

private parseResponse(resp) {    
    log.debug("Executing parseResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
	def temperatureUnit = state.tempunit
    log.debug("Temperature Unit is ${temperatureUnit}")
	def humidityUnit = "%"
    def ACMode
    def ACFanSpeed
    def thermostatSetpoint
	def tOperatingState
    if(resp.status == 200) {
        log.debug("Executing parseResponse.successTrue")
        def temperature
		if (state.supportsWaterTempControl == "true" && resp.data.tadoMode != null && resp.data.setting.power != "OFF"){
			if (temperatureUnit == "C") {
				temperature = (Math.round(resp.data.setting.temperature.celsius * 10 ) / 10)
			}
			else if(temperatureUnit == "F"){
				temperature = (Math.round(resp.data.setting.temperature.fahrenheit * 10) / 10)
			}
			log.debug("Read temperature: " + temperature)
			sendEvent(name: 'temperature', value: temperature, unit: temperatureUnit)
			log.debug("Send Temperature Event Fired")
		} else {
			sendEvent(name: 'temperature', value: "--", unit: temperatureUnit)
			log.debug("Send Temperature Event Fired")
		}
        def autoOperation = "OFF"
        if(resp.data.overlayType == null){
        	autoOperation = resp.data.tadoMode
        }
        else if(resp.data.overlayType == "NO_FREEZE"){
        	autoOperation = "OFF"
        }else if(resp.data.overlayType == "MANUAL"){
        	autoOperation = "MANUAL"
        }
        log.debug("Read tadoMode: " + autoOperation)
        sendEvent(name: 'tadoMode', value: autoOperation)
       
		if (resp.data.setting.power == "ON"){
			sendEvent(name: 'thermostatMode', value: "heat")
			sendEvent(name: 'thermostatOperatingState', value: "heating")
			log.debug("Send thermostatMode Event Fired")
		} else if(resp.data.setting.power == "OFF"){
			sendEvent(name: 'thermostatMode', value: "off")
			sendEvent(name: 'thermostatOperatingState', value: "idle")
			log.debug("Send thermostatMode Event Fired")
		}
        log.debug("Send thermostatMode Event Fired")
		if (state.supportsWaterTempControl == "true" && resp.data.tadoMode != null && resp.data.setting.power != "OFF"){
			if (temperatureUnit == "C") {
				thermostatSetpoint = resp.data.setting.temperature.celsius
			}
			else if(temperatureUnit == "F"){
				thermostatSetpoint = resp.data.setting.temperature.fahrenheit
			}
			log.debug("Read thermostatSetpoint: " + thermostatSetpoint)
		} else {
			thermostatSetpoint = "--"
		}
	}	

	else{
        log.debug("Executing parseResponse.successFalse")
    }

    sendEvent(name: 'thermostatSetpoint', value: thermostatSetpoint, unit: temperatureUnit)
    log.debug("Send thermostatSetpoint Event Fired")
    sendEvent(name: 'heatingSetpoint', value: thermostatSetpoint, unit: temperatureUnit)
    log.debug("Send heatingSetpoint Event Fired")
}

private parseTempResponse(resp) {
    log.debug("Executing parseTempResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
    	log.debug("Executing parseTempResponse.successTrue")
        def tempunitname = resp.data.temperatureUnit
        if (tempunitname == "CELSIUS") {
        	log.debug("Setting Temp Unit to C")
        	state.tempunit = "C"
        }
        else if(tempunitname == "FAHRENHEIT"){
        	log.debug("Setting Temp Unit to F")
        	state.tempunit = "F"
        }       
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}

private parseCapabilitiesResponse(resp) {
    log.debug("Executing parseCapabilitiesResponse: "+resp.data)
    log.debug("Output status: " + resp.status)
    if(resp.status == 200) {
    	try{
    	log.debug("Executing parseResponse.successTrue")
       	state.tadoType = resp.data.type
        log.debug("Tado Type is ${state.tadoType}")

        if(resp.data.type == "HOT_WATER"){
        	log.debug("setting WATER capability state true")
        	state.supportsWater = "true"
			if (resp.data.canSetTemperature == true){
			state.supportsWaterTempControl = "true"
				if (state.tempunit == "C"){
					state.MaxHeatTemp = resp.data.temperatures.celsius.max
					log.debug("set state.MaxHeatTemp to : " + state.MaxHeatTemp + "C")
					state.MinHeatTemp = resp.data.temperatures.celsius.min
					log.debug("set state.MinHeatTemp to : " + state.MinHeatTemp + "C")
				} else if (state.tempunit == "F") {
					state.MaxHeatTemp = resp.data.temperatures.fahrenheit.max
					log.debug("set state.MaxHeatTemp to : " + state.MaxHeatTemp + "F")
					state.MinHeatTemp = resp.data.temperatures.fahrenheit.min
					log.debug("set state.MinHeatTemp to : " + state.MinHeatTemp + "F")
				}   
			} else {
			state.supportsWaterTempControl = "false"
			}
        } else {
        	log.debug("setting HEAT capability state false")
        	state.supportsWater = "false"
        }
        log.debug("state.supportsWater = ${state.supportsWater}")
		log.debug("state.supportsWaterTempControl = ${state.supportsWaterTempControl}")
		
    }catch(Exception e){
        log.debug("___exception: " + e)
    }   
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}


private parseweatherResponse(resp) {
    log.debug("Executing parseweatherResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
	def temperatureUnit = state.tempunit
    log.debug("Temperature Unit is ${temperatureUnit}")
    if(resp.status == 200) {
    	log.debug("Executing parseResponse.successTrue")
        def outsidetemperature
        if (temperatureUnit == "C") {
        	outsidetemperature = resp.data.outsideTemperature.celsius
        }
        else if(temperatureUnit == "F"){
        	outsidetemperature = resp.data.outsideTemperature.fahrenheit
        }
        log.debug("Read outside temperature: " + outsidetemperature)
        sendEvent(name: 'outsidetemperature', value: outsidetemperature , unit: temperatureUnit)
        log.debug("Send Outside Temperature Event Fired")
        
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}


private sendCommand(method, args = []) {
    def methods = [
		'getid': [
        			uri: "https://my.tado.com", 
                    path: "/api/v2/me", 
                    requestContentType: "application/json", 
                    query: [username:settings.username, password:settings.password]
                    ],
        'gettempunit': [
        			uri: "https://my.tado.com", 
                    path: "/api/v2/homes/${state.homeId}", 
                    requestContentType: "application/json", 
                    query: [username:settings.username, password:settings.password]
                    ],
        'getcapabilities': [
        			uri: "https://my.tado.com", 
                    path: "/api/v2/homes/" + state.homeId + "/zones/0/capabilities", 
                    requestContentType: "application/json", 
                    query: [username:settings.username, password:settings.password]
                    ],
        'status': [
        			uri: "https://my.tado.com", 
                    path: "/api/v2/homes/" + state.homeId + "/zones/0/state", 
                    requestContentType: "application/json", 
                    query: [username:settings.username, password:settings.password]
                    ],
		'temperature': [	
        			uri: "https://my.tado.com",
        			path: "/api/v2/homes/" + state.homeId + "/zones/0/overlay",
        			requestContentType: "application/json",
                    query: [username:settings.username, password:settings.password],
                  	body: args[0]
                   	],
		'weatherStatus': [	
        			uri: "https://my.tado.com",
        			path: "/api/v2/homes/" + state.homeId + "/weather",
        			requestContentType: "application/json",
    				query: [username:settings.username, password:settings.password]
                   	],
        'deleteEntry': [	
        			uri: "https://my.tado.com",
        			path: "/api/v2/homes/" + state.homeId + "/zones/0/overlay",
        			requestContentType: "application/json",
                    query: [username:settings.username, password:settings.password],
                   	]
	]

	def request = methods.getAt(method)
    
    log.debug "Http Params ("+request+")"
    
    try{
        log.debug "Executing 'sendCommand'"
        
        if (method == "getid"){
            httpGet(request) { resp ->            
                parseMeResponse(resp)
            }
        }else if (method == "gettempunit"){
            httpGet(request) { resp ->            
                parseTempResponse(resp)
            }
       	}else if (method == "getcapabilities"){
            httpGet(request) { resp ->            
                parseCapabilitiesResponse(resp)
            }
        }else if (method == "status"){
            httpGet(request) { resp ->            
                parseResponse(resp)
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
        }else if (method == "deleteEntry"){
            httpDelete(request) { resp ->            
                parsedeleteResponse(resp)
            }
        }else{
            httpGet(request)
        }
    } catch(Exception e){
        log.debug("___exception: " + e)
    }
}

// Commands to device

def endManualControl(){
	log.debug "Executing 'sendCommand.endManualControl'"
	sendCommand("deleteEntry",[])
	statusCommand()
}

def getidCommand(){
	log.debug "Executing 'sendCommand.getidCommand'"
	sendCommand("getid",[])
}

def getCapabilitiesCommand(){
	log.debug "Executing 'sendCommand.getcapabilities'"
	sendCommand("getcapabilities",[])
}

def getTempUnitCommand(){
	log.debug "Executing 'sendCommand.getidCommand'"
	sendCommand("gettempunit",[])
}

def setThermostatMode(requiredMode){
	switch (requiredMode) {
    	case "heat":
        	heat()
        break
        case "auto":
        	auto()
        break
		case "off":
        	off()
        break
		case "emergency heat":
        	emergencyHeat()
        break
     }
}

def autoCommand(){
	log.debug "Executing 'sendCommand.autoCommand'"
    def initialsetpointtemp
	def terminationmode = settings.manualmode
	def jsonbody
	def traperror
	if(state.supportsWaterTempControl == "true"){ 
		try {
			traperror = ((device.currentValue("thermostatSetpoint")).intValue())
		}catch (NumberFormatException e){
			traperror = 0 
		}
		if(traperror == 0){
			initialsetpointtemp = settings.defHeatingTemp
		} else {
			initialsetpointtemp = device.currentValue("thermostatSetpoint")
		}
		jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", temperature:[celsius:initialsetpointtemp], type:"HOT_WATER"], termination:[type:terminationmode]])
	} else {
		jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", type:"HOT_WATER"], termination:[type:terminationmode]])
	}
	sendCommand("temperature",[jsonbody])
}

def heat(){
	heatCommand()
	refresh()
}

def setHeatingTempCommand(targetTemperature){
    def jsonbody
	def terminationmode = settings.manualmode
	if(state.supportsWaterTempControl == "true"){ 
		if (state.tempunit == "C") {
				jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", temperature:[celsius:targetTemperature], type:"HOT_WATER"], termination:[type:terminationmode]])
			}
			else if(state.tempunit == "F"){
				jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", temperature:[fahrenheit:targetTemperature], type:"HOT_WATER"], termination:[type:terminationmode]])
			}
		log.debug "Executing 'sendCommand.setHeatingTempCommand' to ${targetTemperature}"
		sendCommand("temperature",[jsonbody])
	} else {
		log.debug "Hot Water Temperature Capability Not Supported"
	}
}

def offCommand(){
	log.debug "Executing 'sendCommand.offCommand'"
	def terminationmode = settings.manualmode
    def jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"OFF", type:"HOT_WATER"], termination:[type:terminationmode]])
	sendCommand("temperature",[jsonbody])
}

def onCommand(){
	heatCommand()
}

def heatCommand(){
	log.debug "Executing 'sendCommand.heatCommand'"
	def terminationmode = settings.manualmode
	def jsonbody
    def initialsetpointtemp
	def traperror
	if(state.supportsWaterTempControl == "true"){ 
		try {
			traperror = ((device.currentValue("thermostatSetpoint")).intValue())
		}catch (NumberFormatException e){
			traperror = 0 
		}
		if(traperror == 0){
			initialsetpointtemp = settings.defHeatingTemp
		} else {
			initialsetpointtemp = device.currentValue("thermostatSetpoint")
		}
		jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", temperature:[celsius:initialsetpointtemp], type:"HOT_WATER"], termination:[type:terminationmode]])
	} else {
		jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", type:"HOT_WATER"], termination:[type:terminationmode]])
	}
	sendCommand("temperature",[jsonbody])
}

def emergencyHeat(){
	log.debug "Executing 'sendCommand.heatCommand'"
    def initialsetpointtemp
	def jsonbody
	def traperror
	if(state.supportsWaterTempControl == "true"){ 
	    try {
			traperror = ((device.currentValue("thermostatSetpoint")).intValue())
		}catch (NumberFormatException e){
			traperror = 0 
		}
		if(traperror == 0){
			initialsetpointtemp = settings.defHeatingTemp
		} else {
			initialsetpointtemp = device.currentValue("thermostatSetpoint")
		}
		jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", temperature:[celsius:initialsetpointtemp], type:"HOT_WATER"], termination:[durationInSeconds:"3600", type:"TIMER"]])
	} else {
		jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", type:"HOT_WATER"], termination:[durationInSeconds:"3600", type:"TIMER"]])
	}
	sendCommand("temperature",[jsonbody])
	statusCommand()
}

def statusCommand(){
	log.debug "Executing 'sendCommand.statusCommand'"
	sendCommand("status",[])
}

def weatherStatusCommand(){
	log.debug "Executing 'sendCommand.weatherStatusCommand'"
	sendCommand("weatherStatus",[])
}