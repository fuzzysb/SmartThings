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
 *	Date: 2016-04-09 v2.5 Major bug fix exercise, found lots and lots and lots.....now 100% conforms to ST Thermostat capability. main panel now shows colour of operating state. new attributes tadoMode and tadoFanSpeed created.
 *	Date: 2016-04-05 v2.4 Performed Testing with Thermostat Mode Director and found some deficiencies where this would not work correctly. i have now corrected, this now works fine and has been tested.
 *	Date: 2016-04-05 v2.3 added device preference for default temps for some commands as requested by @mitchell_lu66, also added some additional refreshes and error control for unsupported capabilities
 *	Date: 2016-04-05 v2.2 Added Fan Speed & Emergency Heat (1 Hour) Controls and also a manual Mode End function to fall back to Tado Control. 
 						  Also added preference for how long manual mode runs for either ends at Tado Mode Change (TADO_MODE) or User Control (MANUAL), 
                          please ensure the default method is Set in the device properties
 *	Date: 2016-04-05 v2.1 Minor Bug Fixes & improved Icons
 *	Date: 2016-04-05 v2.0 Further Changes to MultiAttribute Tile
 *	Date: 2016-04-05 v1.9 Amended Device Handler Name
 *	Date: 2016-04-05 v1.8 Added all thermostat related capabilities
 *  Date: 2016-04-05 v1.7 Amended device to be capable of both Fahrenheit and celsius and amended the Device multiattribute tile
 *  Date: 2016-04-05 v1.6 switched API calls to new v2 calls as the old ones had been deprecated.
 *  Date: 2016-02-21 v1.5 switched around thermostatOperatingState & thermostatMode to get better compatibility with Home Remote
 *  Date: 2016-02-21 v1.4 added HeatingSetPoint & CoolingSetPoint to make compatible with SmartTiles
 *  Date: 2016-02-21 v1.3 amended the read thermostat properties to match the ST Thermostat Capability
 *  Date: 2016-02-14 v1.2 amended the thermostat properties to match the ST Capability.Thermostat values
 *  Date: 2016-01-23 v1.1 fixed error in Tado Mode detection
 *	Date: 2016-01-22 v1.1 Add Heating & Cooling Controls (initial offering, will need to look into adding all possible commands)
 *	Date: 2015-12-04 v1.0 Initial Release With Temperatures & Relative Humidity
 */
 
import groovy.json.JsonOutput

preferences {
	input("username", "text", title: "Username", description: "Your Tado username")
	input("password", "password", title: "Password", description: "Your Tado password")
    input("manualmode", "enum", title: "Default Manual Overide Method", options: ["TADO_MODE","MANUAL"], required: false, defaultValue:"TADO_MODE")
    input("defHeatingTemp", "number", title: "Default Heating Temperature?", required: false, defaultValue: 21)
    input("defCoolingTemp", "number", title: "Default Cooling Temperature?", required: false, defaultValue: 21)
}  
 
metadata {
	definition (name: "Tado Cooling Thermostat", namespace: "fuzzysb", author: "Stuart Buchanan") {
		capability "Actuator"
        capability "Temperature Measurement"
		capability "Thermostat Cooling Setpoint"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Mode"
		capability "Thermostat Fan Mode"
		capability "Thermostat Setpoint"
		capability "Thermostat Operating State"
		capability "Thermostat"
		capability "Relative Humidity Measurement"
		capability "Polling"
		capability "Refresh"
        
        attribute "tadoMode", "string"
        attribute "tadoFanSpeed", "string"
        command "temperatureUp"
        command "temperatureDown"
        command "heatingSetpointUp"
        command "heatingSetpointDown"
        command "coolingSetpointUp"
        command "coolingSetpointDown"
        command "dry"
        command "on"
        command "endManualControl"
        command "fanSpeedAuto"
        command "fanSpeedHigh"
        command "fanSpeedMid"
        command "fanSpeedLow"
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
            	attributeState "default", label:'${currentValue}°', backgroundColor:"#fab907", icon:"st.Home.home1"
            }
            tileAttribute("device.temperature", key: "VALUE_CONTROL") {
    			attributeState("VALUE_UP", action: "temperatureUp")
    			attributeState("VALUE_DOWN", action: "temperatureDown")
  			}
			tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
    			attributeState("default", label:'${currentValue}%', unit:"%")
  			}
            tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
    			attributeState("idle", backgroundColor:"#666666")
    			attributeState("heating", backgroundColor:"#ff471a")
    			attributeState("cooling", backgroundColor:"#1a75ff")
                attributeState("emergency heat", backgroundColor:"#ff471a")
                attributeState("drying", backgroundColor:"#c68c53")
                attributeState("fanning", backgroundColor:"#39e600")
                attributeState("heating|cooling", backgroundColor:"#ff9900")
  			}
            tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
    			attributeState("off", label:'${name}')
    			attributeState("heat", label:'${name}')
    			attributeState("cool", label:'${name}')
    			attributeState("auto", label:'${name}')
            	attributeState("fan", label:'${name}')
           		attributeState("dry", label:'${name}')
  			}
            tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
    			attributeState("default", label:'${currentValue}', unit:"dF")
  			}
  			tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
    			attributeState("default", label:'${currentValue}', unit:"dF")
  			}
        
        
 	}
        
        standardTile("tadoMode", "device.tadoMode", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {         
			state("SLEEP", label:'${name}', backgroundColor:"#0164a8", icon:"st.Bedroom.bedroom2")
            state("HOME", label:'${name}', backgroundColor:"#fab907", icon:"st.Home.home2")
            state("AWAY", label:'${name}', backgroundColor:"#62aa12", icon:"st.Outdoor.outdoor18")
            state("OFF", label:'${name}', backgroundColor:"#ffffff", icon:"st.switches.switch.off", defaultState: true)
            state("MANUAL", label:'${name}', backgroundColor:"#804000", icon:"st.Weather.weather1")
		}
    	
        standardTile("refresh", "device.switch", inactiveLabel: false, width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        standardTile("thermostatMode", "device.thermostatMode", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
        	state("heat", label:'HEAT', backgroundColor:"#ea2a2a", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/heat_mode_icon.png")
            state("emergency heat", label:'HEAT', backgroundColor:"#ea2a2a", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/heat_mode_icon.png")
            state("cool", label:'COOL', backgroundColor:"#089afb", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/cool_mode_icon.png")
            state("dry", label:'DRY', icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/dry_mode_icon.png")
            state("fan", label:'FAN', backgroundColor:"#ffffff", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_mode_icononly.png")
            state("auto", label:'AUTO', icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/auto_mode_icon.png")
            state("off", label:'', backgroundColor:"#ffffff", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/hvac_off.png", defaultState: true)  
		}
        
		valueTile("thermostatSetpoint", "device.thermostatSetpoint", width: 2, height: 1, decoration: "flat") {
			state "default", label: 'Set Point\r\n\${currentValue}°'
		}
        
        valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 1, decoration: "flat") {
			state "default", label: 'Set Point\r\n\${currentValue}°'
		}
        
        valueTile("coolingSetpoint", "device.coolingSetpoint", width: 2, height: 1, decoration: "flat") {
			state "default", label: 'Set Point\r\n\${currentValue}°'
		}
		
		valueTile("outsidetemperature", "device.outsidetemperature", width: 2, height: 1, decoration: "flat") {
			state "outsidetemperature", label: 'Outside Temp\r\n${currentValue}°'
		}
       
		standardTile("tadoFanSpeed", "device.tadoFanSpeed", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
        	state("OFF", label:'', icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_off_icon.png", defaultState: true)
            state("AUTO", label:'', icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_auto_icon.png")
            state("HIGH", label:'', icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_high_icon.png")
            state("MIDDLE", label:'', icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_med_icon.png")
            state("LOW", label:'', icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_low_icon.png")       
		}
		standardTile("setAuto", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"thermostat.auto", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/hvac_auto.png"
		}
        standardTile("setDry", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"dry", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/hvac_dry.png"
		}
        standardTile("setOn", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"on", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/hvac_on.png"
		}
        standardTile("setOff", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"thermostat.off", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/hvac_off.png"
		}
        standardTile("cool", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"thermostat.cool", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/hvac_cool.png"
		}
        standardTile("heat", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"thermostat.heat", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/hvac_heat.png"
		}
        standardTile("emergencyHeat", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"thermostat.emergencyHeat", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/emergencyHeat.png"
		}
        standardTile("fan", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"thermostat.fanAuto", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_mode_icon.png"
		}
        standardTile("coolingSetpointUp", "device.coolingSetpoint", canChangeIcon: false, decoration: "flat") {
            state "coolingSetpointUp", label:'  ', action:"coolingSetpointUp", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/cool_arrow_up.png"
        }
		standardTile("coolingSetpointDown", "device.coolingSetpoint", canChangeIcon: false, decoration: "flat") {
            state "coolingSetpointDown", label:'  ', action:"coolingSetpointDown", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/cool_arrow_down.png"
        }
		standardTile("heatingSetpointUp", "device.heatingSetpoint", canChangeIcon: false, decoration: "flat") {
            state "heatingSetpointUp", label:'  ', action:"heatingSetpointUp", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/heat_arrow_up.png"
        }
        standardTile("heatingSetpointDown", "device.heatingSetpoint", canChangeIcon: false, decoration: "flat") {
            state "heatingSetpointDown", label:'  ', action:"heatingSetpointDown", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/heat_arrow_down.png"
        }
		standardTile("SetFanSpeedAuto", "device.tadoFanSpeed", width: 2, height: 1, canChangeIcon: true, canChangeBackground: true, decoration: "flat") {
            state("AUTO", label:'', action:"fanSpeedAuto",  icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_auto_icon.png")
        }
        standardTile("SetFanSpeedHigh", "device.tadoFanSpeed", width: 2, height: 1, canChangeIcon: true, canChangeBackground: true, decoration: "flat") {
            state("AUTO", label:'', action:"fanSpeedHigh",  icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_high_icon.png")
        }
        standardTile("SetFanSpeedMid", "device.tadoFanSpeed", width: 2, height: 1, canChangeIcon: true, canChangeBackground: true, decoration: "flat") {
            state("AUTO", label:'', action:"fanSpeedMid",  icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_med_icon.png")
        }
        standardTile("SetFanSpeedLow", "device.tadoFanSpeed", width: 2, height: 1, canChangeIcon: true, canChangeBackground: true, decoration: "flat") {
            state("AUTO", label:'', action:"fanSpeedLow",  icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/fan_low_icon.png")
        }
		standardTile("endManualControl", "device.thermostat", width: 2, height: 1, canChangeIcon: false, canChangeBackground: true, decoration: "flat") {
            state("default", label:'', action:"endManualControl", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/tado-Cooling-AC.src/Images/endManual.png")
		}
		
		main(["thermostat"])
		details(["thermostat","thermostatMode","coolingSetpointUp","coolingSetpointDown","autoOperation","heatingSetpointUp","heatingSetpointDown","outsidetemperature","thermostatSetpoint","tadoMode","refresh","tadoFanSpeed","setAuto","setOn","setOff","fan","cool","heat","setDry","SetFanSpeedAuto","emergencyHeat","endManualControl","SetFanSpeedLow","SetFanSpeedMid","SetFanSpeedHigh"])
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
    def ACFanMode = "off"
    def thermostatSetpoint
    def tOperatingState
    if(resp.status == 200) {
        log.debug("Executing parseResponse.successTrue")
        def temperature
        if (temperatureUnit == "C") {
        	temperature = (Math.round(resp.data.sensorDataPoints.insideTemperature.celsius *10 ) / 10)
        }
        else if(temperatureUnit == "F"){
        	temperature = (Math.round(resp.data.sensorDataPoints.insideTemperature.fahrenheit * 10) / 10)
        }
        log.debug("Read temperature: " + temperature)
        sendEvent(name: 'temperature', value: temperature, unit: temperatureUnit)
        log.debug("Send Temperature Event Fired")
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
        log.debug("Send thermostatMode Event Fired")

        def humidity 
        if (resp.data.sensorDataPoints.humidity.percentage != null){
        	humidity = resp.data.sensorDataPoints.humidity.percentage
        }else{
        	humidity = "--"
        }
        log.debug("Read humidity: " + humidity)
			       
        sendEvent(name: 'humidity', value: humidity,unit: humidityUnit)

    	if (resp.data.setting.power == "OFF"){
            tOperatingState = "idle"
       		ACMode = "off"
            ACFanMode = "off"
        	log.debug("Read thermostatMode: " + ACMode)
			ACFanSpeed = "OFF"
        	log.debug("Read tadoFanSpeed: " + ACFanSpeed)
			thermostatSetpoint = 0
        	log.debug("Read thermostatSetpoint: " + thermostatSetpoint)
    	}
   	 	else if (resp.data.setting.power == "ON"){
       		ACMode = (resp.data.setting.mode).toLowerCase()
			log.debug("thermostatMode: " + ACMode)
			ACFanSpeed = resp.data.setting.fanSpeed
			if (ACFanSpeed == null) {
				ACFanSpeed = "--"
            }
            if (resp.data.overlay.termination.type == "TIMER" && resp.data.overlay.termination.durationInSeconds == "3600"){ 
            	ACMode = "emergency heat"
                log.debug("thermostatMode is heat, however duration shows the state is: " + ACMode)
            }
            switch (ACMode) {
    			case "heat":
        			tOperatingState = "heating"
        		break
    			case "emergency heat":
        			tOperatingState = "heating"
        		break
        		case "cool":
        			tOperatingState = "cooling"
        		break
                case "dry":
        			tOperatingState = "drying"
        		break
                case "fan":
        			tOperatingState = "fanning"
        		break
                case "auto":
        			tOperatingState = "heating|cooling"
        		break
			}
            log.debug("Read thermostatOperatingState: " + tOperatingState)
        	log.debug("Read tadoFanSpeed: " + ACFanSpeed)
        
        if (ACMode == "dry" || ACMode == "auto" || ACMode == "fan"){
        	thermostatSetpoint = "--"
        }else if(ACMode == "fan") {
        	ACFanMode = "auto"      
        }else{
       		if (temperatureUnit == "C") {
        		thermostatSetpoint = Math.round(resp.data.setting.temperature.celsius)
        	}
        	else if(temperatureUnit == "F"){
        		thermostatSetpoint = Math.round(resp.data.setting.temperature.fahrenheit)
        	}
        }
        log.debug("Read thermostatSetpoint: " + thermostatSetpoint)
      }
    }else{
        log.debug("Executing parseResponse.successFalse")
    }
    sendEvent(name: 'thermostatOperatingState', value: tOperatingState)
    log.debug("Send thermostatOperatingState Event Fired")
	sendEvent(name: 'tadoFanSpeed', value: ACFanSpeed)
    log.debug("Send tadoFanSpeed Event Fired")
    sendEvent(name: 'thermostatFanMode', value: ACFanMode)
    log.debug("Send thermostatFanMode Event Fired")
	sendEvent(name: 'thermostatMode', value: ACMode)
    log.debug("Send thermostatMode Event Fired")
    sendEvent(name: 'thermostatSetpoint', value: thermostatSetpoint, unit: temperatureUnit)
    log.debug("Send thermostatSetpoint Event Fired")
    sendEvent(name: 'heatingSetpoint', value: thermostatSetpoint, unit: temperatureUnit)
    log.debug("Send heatingSetpoint Event Fired")
    sendEvent(name: 'coolingSetpoint', value: thermostatSetpoint, unit: temperatureUnit)
    log.debug("Send coolingSetpoint Event Fired")

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
        if(resp.data.AUTO || (resp.data.AUTO).toString() == "[:]"){
        	log.debug("settingautocapability state true")
        	state.supportsAuto = "true"
        } else {
        	log.debug("settingautocapability state false")
        	state.supportsAuto = "false"
        }
        if(resp.data.COOL || (resp.data.COOL).toString() == "[:]"){
        	log.debug("setting COOL capability state true")
        	state.supportsCool = "true"
            def coolfanmodelist = resp.data.COOL.fanSpeeds
            if(coolfanmodelist.find { it == 'AUTO' }){
            	log.debug("setting COOL Auto Fan Speed capability state true")
            	state.SupportsCoolAutoFanSpeed = "true"
            } else {
            	log.debug("setting COOL Auto Fan Speed capability state false")
            	state.SupportsCoolAutoFanSpeed = "false"
            }
            if (state.tempunit == "C"){
            	state.MaxCoolTemp = resp.data.COOL.temperatures.celsius.max
                log.debug("set state.MaxCoolTemp to : " + state.MaxCoolTemp + "C")
                state.MinCoolTemp = resp.data.COOL.temperatures.celsius.min
                log.debug("set state.MinCoolTemp to : " + state.MinCoolTemp + "C")
            } else if (state.tempunit == "F") {
            	state.MaxCoolTemp = resp.data.COOL.temperatures.fahrenheit.max
                log.debug("set state.MaxCoolTemp to : " + state.MaxCoolTemp + "F")
                state.MinCoolTemp = resp.data.COOL.temperatures.fahrenheit.min
                log.debug("set state.MinCoolTemp to : " + state.MinCoolTemp + "F")
           	}    
        } else {
        	log.debug("setting COOL capability state false")
        	state.supportsCool = "false"
        }
        if(resp.data.DRY || (resp.data.DRY).toString() == "[:]"){
        	log.debug("setting DRY capability state true")
        	state.supportsDry = "true"
        } else {
        	log.debug("setting DRY capability state false")
        	state.supportsDry = "false"
        }
        if(resp.data.FAN || (resp.data.FAN).toString() == "[:]"){
        	log.debug("setting FAN capability state true")
        	state.supportsFan = "true"
        } else {
        	log.debug("setting FAN capability state false")
        	state.supportsFan = "false"
        }
        if(resp.data.HEAT || (resp.data.HEAT).toString() == "[:]"){
        	log.debug("setting HEAT capability state true")
        	state.supportsHeat = "true"
            def heatfanmodelist = resp.data.HEAT.fanSpeeds
            if(heatfanmodelist.find { it == 'AUTO' }){
            	log.debug("setting HEAT Auto Fan Speed capability state true")
            	state.SupportsHeatAutoFanSpeed = "true"
            } else {
            	log.debug("setting HEAT Auto Fan Speed capability state false")
            	state.SupportsHeatAutoFanSpeed = "false"
            }
            if (state.tempunit == "C"){
            	state.MaxHeatTemp = resp.data.HEAT.temperatures.celsius.max
                log.debug("set state.MaxHeatTemp to : " + state.MaxHeatTemp + "C")
                state.MinHeatTemp = resp.data.HEAT.temperatures.celsius.min
                log.debug("set state.MinHeatTemp to : " + state.MinHeatTemp + "C")
            } else if (state.tempunit == "F") {
            	state.MaxHeatTemp = resp.data.HEAT.temperatures.fahrenheit.max
                log.debug("set state.MaxHeatTemp to : " + state.MaxHeatTemp + "F")
                state.MinHeatTemp = resp.data.HEAT.temperatures.fahrenheit.min
                log.debug("set state.MinHeatTemp to : " + state.MinHeatTemp + "F")
           	}    
        } else {
        	log.debug("setting HEAT capability state false")
        	state.supportsHeat = "false"
        }
        log.debug("state.supportsDry = ${state.supportsDry}")
        log.debug("state.supportsCool = ${state.supportsCool}")
        log.debug("state.supportsFan = ${state.supportsFan}")
        log.debug("state.supportsAuto = ${state.supportsAuto}")
        log.debug("state.supportsHeat = ${state.supportsHeat}")
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

def updated(){
def cmds = [
getidCommand(),
getTempUnitCommand(),
getCapabilitiesCommand()
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

def dry() {
	log.debug "Executing 'dry'"
	dryCommand()
    statusCommand()
}

def setThermostatMode(requiredMode){
	switch (requiredMode) {
    	case "dry":
        	dry()
        break
    	case "heat":
        	heat()
        break
        case "cool":
        	cool()
        break
        case "auto":
        	auto()
        break
        case "fan":
        	fanAuto()
        break
		case "off":
        	off()
        break
		case "emergency heat":
        	emergencyHeat()
        break
     }
}

def thermostatFanMode(requiredMode){
	switch (requiredMode) {
    	case "auto":
        	fanAuto()
        break
    	case "on":
        	fanAuto()
        break
        case "circulate":
        	fanAuto()
        break
     }
}




def setHeatingSetpoint(targetTemperature) {
	log.debug "Executing 'setHeatingSetpoint'"
    log.debug "Target Temperature ${targetTemperature}"
    setHeatingTempCommand(targetTemperature)
	refresh()
}

def temperatureUp(){
	if (device.currentValue("thermostatMode") == "heat") {
    	heatingSetpointUp()
    } else if (device.currentValue("thermostatMode") == "cool") {
    	coolingSetpointUp()
    } else {
    	log.debug ("temperature setpoint not supported in the current thermostat mode")
    }
}

def temperatureDown(){
	if (device.currentValue("thermostatMode") == "heat") {
    	heatingSetpointDown()
    } else if (device.currentValue("thermostatMode") == "cool") {
    	coolingSetpointDown()
    } else {
    	log.debug ("temperature setpoint not supported in the current thermostat mode")
    }
}



def heatingSetpointUp(){
	def capabilitysupported = state.supportsHeat
    if (capabilitysupported == "true"){
		log.debug "Current SetPoint Is " + (device.currentValue("thermostatSetpoint")).toString()
    	if ((device.currentValue("thermostatSetpoint").toInteger() - 1 ) < state.MinHeatTemp){
    		log.debug("cannot decrease heat setpoint, its already at the minimum level of " + state.MinHeatTemp)
    	} else {
			int newSetpoint = (device.currentValue("thermostatSetpoint")).toInteger() + 1
			log.debug "Setting heatingSetpoint up to: ${newSetpoint}"
			setHeatingSetpoint(newSetpoint)
        	statusCommand()
    	}
    } else {
    	log.debug("Sorry Heat Capability not supported by your HVAC Device")
    }
}

def heatingSetpointDown(){
	def capabilitysupported = state.supportsHeat
    if (capabilitysupported == "true"){
		log.debug "Current SetPoint Is " + (device.currentValue("thermostatSetpoint")).toString()
    	if ((device.currentValue("thermostatSetpoint").toInteger() + 1 ) > state.MaxHeatTemp){
    		log.debug("cannot increase heat setpoint, its already at the maximum level of " + state.MaxHeatTemp)
    	} else {
			int newSetpoint = (device.currentValue("thermostatSetpoint")).toInteger() - 1
			log.debug "Setting heatingSetpoint down to: ${newSetpoint}"
			setHeatingSetpoint(newSetpoint)
        	statusCommand()
    	}
    } else {
    	log.debug("Sorry Heat Capability not supported by your HVAC Device")
    }
}

def setCoolingSetpoint(targetTemperature) {
	log.debug "Executing 'setCoolingSetpoint'"
    log.debug "Target Temperature ${targetTemperature}"
    setCoolingTempCommand(targetTemperature)
	refresh()
}

def coolingSetpointUp(){
	def capabilitysupported = state.supportsCool
    if (capabilitysupported == "true"){
		log.debug "Current SetPoint Is " + (device.currentValue("thermostatSetpoint")).toString()
    	if ((device.currentValue("thermostatSetpoint").toInteger() + 1 ) > state.MaxCoolTemp){
    		log.debug("cannot increase cool setpoint, its already at the maximum level of " + state.MaxCoolTemp)
    	} else {
			int newSetpoint = (device.currentValue("thermostatSetpoint")).toInteger() + 1
			log.debug "Setting coolingSetpoint up to: ${newSetpoint}"
			setCoolingSetpoint(newSetpoint)
        	statusCommand()
    	}
    } else {
    	log.debug("Sorry Cool Capability not supported by your HVAC Device")
    }
}

def coolingSetpointDown(){
	def capabilitysupported = state.supportsCool
    if (capabilitysupported == "true"){
		log.debug "Current SetPoint Is " + (device.currentValue("thermostatSetpoint")).toString()
    	if ((device.currentValue("thermostatSetpoint").toInteger() - 1 ) < state.MinCoolTemp){
    		log.debug("cannot decrease cool setpoint, its already at the minimum level of " + state.MinCoolTemp)
    	} else {
			int newSetpoint = (device.currentValue("thermostatSetpoint")).toInteger() - 1
			log.debug "Setting coolingSetpoint down to: ${newSetpoint}"
			setCoolingSetpoint(newSetpoint)
        	statusCommand()
    	}
    } else {
    	log.debug("Sorry Cool Capability not supported by your HVAC Device")
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
                    path: "/api/v2/homes/" + state.homeId + "/zones/1/capabilities", 
                    requestContentType: "application/json", 
                    query: [username:settings.username, password:settings.password]
                    ],
        'status': [
        			uri: "https://my.tado.com", 
                    path: "/api/v2/homes/" + state.homeId + "/zones/1/state", 
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
        			path: "/api/v2/homes/" + state.homeId + "/weather",
        			requestContentType: "application/json",
    				query: [username:settings.username, password:settings.password]
                   	],
        'deleteEntry': [	
        			uri: "https://my.tado.com",
        			path: "/api/v2/homes/" + state.homeId + "/zones/1/overlay",
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

def autoCommand(){
    def capabilitysupported = state.supportsAuto
    if (capabilitysupported == "true"){
		log.debug "Executing 'sendCommand.autoCommand'"
    	def terminationmode = settings.manualmode
		def jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"AUTO", power:"ON", type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
		sendCommand("temperature",[jsonbody])
    	statusCommand()
    } else {
    	log.debug("Sorry Auto Capability not supported by your HVAC Device")
    }
}

def dryCommand(){
    def capabilitysupported = state.supportsDry
    if (capabilitysupported == "true"){
		def terminationmode = settings.manualmode
		log.debug "Executing 'sendCommand.dryCommand'"
    	def jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"DRY", power:"ON", type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
		sendCommand("temperature",[jsonbody])
    	statusCommand()
    } else {
    	log.debug("Sorry Dry Capability not supported by your HVAC Device")
    }
}

def fanAuto(){
    def capabilitysupported = state.supportsFan
    if (capabilitysupported == "true"){
		def terminationmode = settings.manualmode
		log.debug "Executing 'sendCommand.fanAutoCommand'"
    	def jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"FAN", power:"ON", type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
		sendCommand("temperature",[jsonbody])
    	statusCommand()
	} else {
    	log.debug("Sorry Fan Capability not supported by your HVAC Device")
    }
}

def fanOn(){
	fanAuto()
}

def fanCirculate(){
	fanAuto()
}

def cool(){
	def capabilitysupported = state.supportsCool
    if (capabilitysupported == "true"){
		coolCommand()
    	statusCommand()
	} else {
    	log.debug("Sorry Cool Capability not supported by your HVAC Device")
    }
}

def heat(){
	def capabilitysupported = state.supportsHeat
    if (capabilitysupported == "true"){
		heatCommand()
        statusCommand()
    } else {
    	log.debug("Sorry Heat Capability not supported by your HVAC Device")
    }
}


def endManualControl(){
	log.debug "Executing 'sendCommand.endManualControl'"
	sendCommand("deleteEntry",[])
    statusCommand()
}


def fanSpeedAuto(){
    def supportedfanspeed
    def terminationmode = settings.manualmode
    def jsonbody
    if (state.SupportsCoolAutoFanSpeed == "true"){
    	supportedfanspeed = "AUTO"
    } else {
        supportedfanspeed = "HIGH"
    } 
	def curSetTemp = (device.currentValue("thermostatSetpoint"))
	def curMode = (device.currentValue("thermostatMode"))
	if (curMode == "COOL" || curMode == "HEAT"){
		if (state.tempunit == "C") {
        	jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", temperature:[celsius:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }
        else if(state.tempunit == "F"){
            jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", temperature:[fahrenheit:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }
		log.debug "Executing 'sendCommand.fanSpeedAuto' to ${supportedfanspeed}"
		sendCommand("temperature",[jsonbody])
        statusCommand()
	}
}

def fanSpeedHigh(){
    def jsonbody
    def supportedfanspeed = "HIGH"
    def terminationmode = settings.manualmode
	def curSetTemp = (device.currentValue("thermostatSetpoint"))
	def curMode = (device.currentValue("thermostatMode"))
	if (curMode == "COOL" || curMode == "HEAT"){
		if (state.tempunit == "C") {
        	jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", temperature:[celsius:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }
        else if(state.tempunit == "F"){
            jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", temperature:[fahrenheit:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }
		log.debug "Executing 'sendCommand.fanSpeedAuto' to ${supportedfanspeed}"
		sendCommand("temperature",[jsonbody])
        statusCommand()
	}
}

def fanSpeedMid(){
    def supportedfanspeed = "MIDDLE"
    def terminationmode = settings.manualmode
    def jsonbody
	def curSetTemp = (device.currentValue("thermostatSetpoint"))
	def curMode = (device.currentValue("thermostatMode"))
	if (curMode == "COOL" || curMode == "HEAT"){
		if (state.tempunit == "C") {
        	jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", temperature:[celsius:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }
        else if(state.tempunit == "F"){
            jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", temperature:[fahrenheit:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }
		log.debug "Executing 'sendCommand.fanSpeedMid' to ${supportedfanspeed}"
		sendCommand("temperature",[jsonbody])
        statusCommand()
	}
}

def fanSpeedLow(){
    def supportedfanspeed = "LOW"
    def terminationmode = settings.manualmode
    def jsonbody
	def curSetTemp = (device.currentValue("thermostatSetpoint"))
	def curMode = (device.currentValue("thermostatMode"))
	if (curMode == "COOL" || curMode == "HEAT"){
		if (state.tempunit == "C") {
        	jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", temperature:[celsius:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }
        else if(state.tempunit == "F"){
            jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", temperature:[fahrenheit:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }
		log.debug "Executing 'sendCommand.fanSpeedLow' to ${supportedfanspeed}"
		sendCommand("temperature",[jsonbody])
        statusCommand()
	}
}

def setCoolingTempCommand(targetTemperature){
    def supportedfanspeed
    def terminationmode = settings.manualmode
    def jsonbody
    if (state.SupportsCoolAutoFanSpeed == "true"){
    	supportedfanspeed = "AUTO"
    } else {
        supportedfanspeed = "HIGH"
    }  
 	if (state.tempunit == "C") {
        	jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"COOL", power:"ON", temperature:[celsius:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }
        else if(state.tempunit == "F"){
            jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"COOL", power:"ON", temperature:[fahrenheit:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }
	log.debug "Executing 'sendCommand.setCoolingTempCommand' to ${targetTemperature}"
	sendCommand("temperature",[jsonbody])
}

def setHeatingTempCommand(targetTemperature){
    def supportedfanspeed
    def terminationmode = settings.manualmode
    def jsonbody
    if (state.SupportsHeatAutoFanSpeed == "true"){
    	supportedfanspeed = "AUTO"
        } else {
        supportedfanspeed = "HIGH"
        }  
 	if (state.tempunit == "C") {
        	jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"HEAT", power:"ON", temperature:[celsius:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }
        else if(state.tempunit == "F"){
        	jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"HEAT", power:"ON", temperature:[fahrenheit:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }
	log.debug "Executing 'sendCommand.setHeatingTempCommand' to ${targetTemperature}"
	sendCommand("temperature",[jsonbody])
}

def offCommand(){
	log.debug "Executing 'sendCommand.offCommand'"
    def terminationmode = settings.manualmode
    def jsonbody = new groovy.json.JsonOutput().toJson([setting:[type:"AIR_CONDITIONING", power:"OFF"], termination:[type:terminationmode]])
	sendCommand("temperature",[jsonbody])
}

def onCommand(){
	def terminationmode = settings.manualmode
    def supportedfanspeed
    def initialsetpointtemp = settings.defCoolingTemp
    if (state.SupportsHeatAutoFanSpeed == "true"){
    	supportedfanspeed = "AUTO"
        } else {
        supportedfanspeed = "HIGH"
        }  
	log.debug "Executing 'sendCommand.onCommand'"
    def jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"COOL", power:"ON", temperature:[celsius:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
	sendCommand("temperature",[jsonbody])
}

def coolCommand(){
	log.debug "Executing 'sendCommand.coolCommand'"
    def terminationmode = settings.manualmode
    def initialsetpointtemp
    def supportedfanspeed
    def traperror
    try {
        traperror = Integer.parseInt(device.currentValue("thermostatSetpoint"))
    }catch (NumberFormatException e){
         traperror = 0 
    }
    if (state.SupportsCoolAutoFanSpeed == "true"){
    	supportedfanspeed = "AUTO"
        } else {
        supportedfanspeed = "HIGH"
        }  
    if(traperror == 0){
    	initialsetpointtemp = settings.defCoolingTemp
    } else {
    	initialsetpointtemp = device.currentValue("thermostatSetpoint")
    }
    def jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"COOL", power:"ON", temperature:[celsius:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
	sendCommand("temperature",[jsonbody])
}

def heatCommand(){
	log.debug "Executing 'sendCommand.heatCommand'"
    def terminationmode = settings.manualmode
    def initialsetpointtemp
    def supportedfanspeed
    def traperror
    try {
        traperror = Integer.parseInt(device.currentValue("thermostatSetpoint"))
    }catch (NumberFormatException e){
         traperror = 0 
    }
    if (state.SupportsHeatAutoFanSpeed == "true"){
    	supportedfanspeed = "AUTO"
        } else {
        supportedfanspeed = "HIGH"
        }
    if(traperror == 0){
    	initialsetpointtemp = settings.defHeatingTemp
    } else {
    	initialsetpointtemp = device.currentValue("thermostatSetpoint")
    }
    def jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"HEAT", power:"ON", temperature:[celsius:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
	sendCommand("temperature",[jsonbody])
}

def emergencyHeat(){
	log.debug "Executing 'sendCommand.heatCommand'"
    def traperror
    def capabilitysupported = state.supportsHeat
    
    try {
        traperror = Integer.parseInt(device.currentValue("thermostatSetpoint"))
    }catch (NumberFormatException e){
         traperror = 0 
    }
    if (capabilitysupported == "true"){
	    def initialsetpointtemp
    	def supportedfanspeed
    	if (state.SupportsHeatAutoFanSpeed == "true"){
    		supportedfanspeed = "AUTO"
     	   } else {
     	   	supportedfanspeed = "HIGH"
     	   }  
    	if(traperror == 0){
    		initialsetpointtemp = settings.defHeatingTemp
    	} else {
    		initialsetpointtemp = device.currentValue("thermostatSetpoint")
    	}
    	def jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"HEAT", power:"ON", temperature:[celsius:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[durationInSeconds:"3600", type:"TIMER"]])
		sendCommand("temperature",[jsonbody])
    	statusCommand()
	} else {
    	log.debug("Sorry Heat Capability not supported by your HVAC Device")
    }
}

def statusCommand(){
	log.debug "Executing 'sendCommand.statusCommand'"
	sendCommand("status",[])
}

def weatherStatusCommand(){
	log.debug "Executing 'sendCommand.weatherStatusCommand'"
	sendCommand("weatherStatus",[])
}