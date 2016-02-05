/**
 *  Garadget Integration
 *
 *  Copyright 2016 Stuart Buchanan based loosely based on original code by Krishnaraj Varma with thanks
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
 */
 
import groovy.json.JsonOutput
 
preferences {
    input("deviceId", "text", title: "Device ID")
    input("token", "text", title: "Access Token")
}

metadata {
	definition (name: "Garadget", namespace: "fuzzysb", author: "Stuart Buchanan") {
			
        capability "Switch"
		capability "Contact Sensor"
		capability "Actuator"
		capability "Sensor"
        capability "Refresh"
        capability "Polling"
		
        
        attribute "status", "string"
        attribute "lastDoorAction", "string"
		
		command "statusCommand"
		command "setConfigCommand"
		command "doorConfigCommand"
		command "netConfigCommand"
	}

	simulator {
		
	}

tiles(scale: 2) {
    multiAttributeTile(name:"status", type: "generic", width: 6, height: 4){
        tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
            attributeState "Open", label:'${name}', action:"status.off", icon:"st.contact.contact.open", backgroundColor:"#ffa81e"
            attributeState "Closing", label:'${name}', icon:"st.doors.garage.garage-closing", backgroundColor:"#6699ff"
            attributeState "Closed", label:'${name}', action:"status.on", icon:"st.doors.garage.garage-closed", backgroundColor:"#79b821"
        }
		tileAttribute ("device.lastaction", key: "SECONDARY_CONTROL") {
				attributeState "default", label: 'last action: ${currentValue}'
		}
    }
	standardTile("contact", "device.contact") {
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e")
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
		}
    standardTile("refresh", "device.temperature", inactiveLabel: false, decoration: "flat") {
            state "default", action:"polling.poll", icon:"st.secondary.refresh"
        }
        
        main "status"
		details(["status", "contact", "lastaction", "refresh"])
	}
}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
    refresh()
}

def refresh() {
	log.debug "Executing 'refresh'"
    statusCommand()
    
}


// Parse incoming device messages to generate events
private parseDoorStatusResponse(resp) {
    log.debug("Executing parseResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
        
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}

private parseDoorConfigResponse(resp) {
    log.debug("Executing parseResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
        
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}

private parseNetConfigResponse(resp) {
    log.debug("Executing parseResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
        
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}

private parseResponse(resp) {
    log.debug("Executing parseResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
        log.debug("Executing parseResponse.successTrue")
        def id = resp.data.id
        def name = resp.data.name
        def connected = resp.data.connected
		def returnValue = resp.data.return_value	
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}


private sendCommand(method, args = []) {
	def DefaultUri = "https://api.particle.io"
    def methods = [
		'doorStatus': [
					uri: "${DefaultUri}",
					path: "/v1/devices/${settings.deviceId}/doorStatus",
					requestContentType: "application/json",
					body: [access_token: settings.token]  
                    ],
        'doorConfig': [
					uri: "${DefaultUri}",
					path: "/v1/devices/${settings.deviceId}/doorConfig",
					requestContentType: "application/json",
					body: [access_token: settings.token] 
                    ],
		'netConfig': [	
					uri: "${DefaultUri}",
					path: "/v1/devices/${settings.deviceId}/netConfig",
					requestContentType: "application/json",
					body: [access_token: settings.token] 
                   	],
		'setState': [	
					uri: "${DefaultUri}",
					path: "/v1/devices/${settings.deviceId}/setState",
					requestContentType: "application/json",
					body: [arg:args[0] , access_token: settings.token] 
                   	]
		'setConfig': [	
					uri: "${DefaultUri}",
					path: "/v1/devices/${settings.deviceId}/setConfig",
					requestContentType: "application/json",
					body: [arg:args[0] , access_token: settings.token] 
                   	]
	]

	def request = methods.getAt(method)
    
    log.debug "Http Params ("+request+")"
    
    try{
        log.debug "Executing 'sendCommand'"
        
        if (method == "doorStatus"){
            httpPost(request) { resp ->            
                parseDoorStatusResponse(resp)
            }
        }else if (method == "doorConfig"){
			log.debug "calling doorConfig Method"
            httpPost(request) { resp ->            
                parseDoorConfigResponse(resp)
            }
		}else if (method == "netConfig"){
			log.debug "calling netConfig Method"
            httpPost(request) { resp ->            
                parseNetConfigResponse(resp)
            }
        }else if (method == "setState"){
            log.debug "calling setState Method"
            httpGet(request) { resp ->            
                parseResponse(resp)
			}
        }else if (method == "setConfig"){
            log.debug "calling setState Method"
            httpGet(request) { resp ->            
                 parseResponse(resp)
            }
        }else{
            httpGet(request)
        }
    } catch(Exception e){
        debug("___exception: " + e)
    }
}


def on() {
	log.debug "Executing 'on'"
	openCommand()
    refresh()
}

def off() {
	log.debug "Executing 'off'"
	closeCommand()
    refresh()
}

def statusCommand(){
	log.debug "Executing 'sendCommand.statusCommand'"
	sendCommand("doorStatus",[])
}

def openCommand(){
	log.debug "Executing 'sendCommand.setState'"
	def jsonbody = new groovy.json.JsonOutput().toJson([arg:"open"])
	sendCommand("setState",[jsonbody])
}

def closeCommand(){
	log.debug "Executing 'sendCommand.setState'"
    def jsonbody = new groovy.json.JsonOutput().toJson([arg:"closed"])
	sendCommand("setState",[jsonbody])
}

def doorConfigCommand(){
	log.debug "Executing 'sendCommand.doorConfig'"
	sendCommand("doorConfig",[])
}

def SetConfigCommand(){
	log.debug "Executing 'sendCommand.setConfig'"
	def jsonbody = new groovy.json.JsonOutput().toJson([arg:"rdt=1000|mtt=10000|mot=2000|rlt=300|rlp=1000|srr=3|srt=25|aot=320|ans=1320|ane=360"])
	sendCommand("setConfig",[jsonbody])
}

def netConfigCommand(){
	log.debug "Executing 'sendCommand.netConfig'"
	sendCommand("netConfig",[])
}