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
 *	Author: Stuart Buchanan, Based on original work by Ian M with thanks
 *	Date: 2015-12-04
 */
 


preferences {
	input("username", "text", title: "Username", description: "Your Tado username")
	input("password", "password", title: "Password", description: "Your Tado password")
}  
 
metadata {
	definition (name: "Tado AC Thermostat", namespace: "fuzzysb", author: "Stuart Buchanan") {
		capability "Actuator"
        capability "Temperature Measurement"
		capability "Thermostat"
        capability "Relative Humidity Measurement"
		capability "Polling"
		capability "Refresh"
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
		}
        
        standardTile("autoOperation", "device.autoOperation", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {         
			state("SLEEP", label:'${name}', backgroundColor:"#0164a8", icon:"st.Bedroom.bedroom2")
            state("HOME", label:'${name}', backgroundColor:"#fab907", icon:"st.Home.home2")
            state("AWAY", label:'${name}', backgroundColor:"#62aa12", icon:"st.Outdoor.outdoor18")
            state("OFF", label:'${name}', backgroundColor:"#ffffff", icon:"st.switches.switch.off", defaultState: true)
            state("MANUAL", label:'${name}', backgroundColor:"#ffffff", icon:"st.Weather.weather1")
		}
    	
        standardTile("refresh", "device.switch", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
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
        
		valueTile("setPointTemp", "device.setPointTemp", width: 2, height: 2, decoration: "flat") {
			state "setPointTemp", label: '${currentValue}° C'
		}
        
       valueTile("Humidity", "device.Humidity", width: 2, height: 2, decoration: "flat") {
			state "CurrentHumidity", label: '${currentValue}% Humidity'
		}
        
		standardTile("ACFanSpeed", "device.ACFanSpeed", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
        	state("OFF", label:'${name}', backgroundColor:"#ffffff", icon:"st.Appliances.appliances11", defaultState: true)
            state("AUTO", label:'${name}', backgroundColor:"#ffffff", icon:"st.Appliances.appliances11")
            state("HIGH", label:'${name}', backgroundColor:"#ffffff", icon:"st.Appliances.appliances11")
            state("MIDDLE", label:'${name}', backgroundColor:"#ffffff", icon:"st.Appliances.appliances11")
            state("LOW", label:'${name}', backgroundColor:"#ffffff", icon:"st.Appliances.appliances11")       
		}
		
		main(["thermostat"])
		details(["thermostat","refresh","Humidity","autoOperation","ACMode","setPointTemp","ACFanSpeed"])
	}
}

// Parse incoming HomeId to poll for  Settings
private homeIdResponse(resp) {
	//def homeid
    log.debug("Executing homeIdResponse: "+resp.data)
    //log.debug("Output success: "+resp.data.success)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
    	log.debug("Executing homeIdResponse.successTrue")
		state.homeId = resp.data.homeId
        log.debug("Got HomeID Value: " + state.homeId)
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
   sendEvent(name: 'homeId', value: homeId)
}

// Parse incoming device messages to generate events
private parseResponse(resp) {
	def temperatureUnit = "C"
    def humidityUnit = "%"
    log.debug("Executing parseResponse: "+resp.data)
    //log.debug("Output success: "+resp.data.success)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
    	log.debug("Executing parseResponse.successTrue")
        
        def temperature = Math.round(resp.data.insideTemperature.celsius)
        log.debug("Read temperature: " + temperature)
        sendEvent(name: 'temperature', value: temperature, unit: temperatureUnit)
        log.debug("Send Temerature Event Fired")
        
        def Humidity = Math.round(resp.data.humidity.percentage)
        log.debug("Read Humidity: " + Humidity)
        sendEvent(name: 'Humidity', value: Humidity, unit: humidityUnit)
        log.debug("Send Humidity Event Fired")
         
        def autoOperation = resp.data.tadoMode
        if (resp.data.tadoMode == "NO_FREEZE"){
        	autoOperation = "OFF"   
        }
        log.debug("Read autoOperation: " + autoOperation)
        sendEvent(name: 'autoOperation', value: autoOperation)
        log.debug("Send autoOperation Event Fired")

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
			setPointTemp = Math.round(resp.data.acSetting.temperature.celsius)
        	log.debug("Read setPointTemp: " + setPointTemp)
        }
        sendEvent(name: 'ACFanSpeed', value: ACFanSpeed)
        log.debug("Send ACFanSpeed Event Fired")
		sendEvent(name: 'ACMode', value: ACMode)
        log.debug("Send ACMode Event Fired")
        sendEvent(name: 'setPointTemp', value: setPointTemp, unit: temperatureUnit)
        log.debug("Send setPointTemp Event Fired")
        
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
    sendCommand("getCurrentState")
}




private sendCommand(path, method="GET", body=null) {
     def homeidParams = [
        uri: "https://my.tado.com",
        path: "/api/v1/me",
        requestContentType: "application/json",
    	query: [username:settings.username, password:settings.password],
        body: body
    ]
    log.debug(method+" Http Params ("+pollParams+")")

    try{
        if(method=="GET"){
        	log.debug "Executing 'sendCommand.GET'"
            httpGet(homeidParams) { resp ->            
                //log.debug resp.data
                homeIdResponse(resp)
            }
        }else if(method=="PUT") {
        	log.debug "Executing 'sendCommand.PUT'"
            httpPut(homeidParams) { resp ->            
                homeIdResponse(resp)
            }
        }
    } catch(Exception e){
        debug("___exception: " + e)
    }
 
      def pollParams = [
        uri: "https://my.tado.com",
        path: "/api/v1/home/" + state.homeId + "/hvacState",
        requestContentType: "application/json",
    	query: [username:settings.username, password:settings.password],
        body: body
    ]
    log.debug(method+" Http Params ("+pollParams+")")
    
    try{
        if(method=="GET"){
        	log.debug "Executing 'sendCommand.GET'"
            httpGet(pollParams) { resp ->            
                //log.debug resp.data
                parseResponse(resp)
            }
        }else if(method=="PUT") {
        	log.debug "Executing 'sendCommand.PUT'"
            httpPut(pollParams) { resp ->            
                parseResponse(resp)
            }
        }
    } catch(Exception e){
        debug("___exception: " + e)
    }
}



// Commands to device