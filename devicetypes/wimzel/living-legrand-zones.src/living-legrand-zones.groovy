/**
 *  Living Legrand Zones
 *
 *  Copyright 2016 Alain Mena Galindo
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
 
metadata {

	definition (name: "Living Legrand Zones", namespace: "wimzel", author: "Alain Mena Galindo") {
		capability "Actuator"
		capability "Switch"
		capability "Switch Level" // brightness
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
	}

	simulator {}

	tiles(scale: 2) {
    
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "unreachable", label: "★", action:"refresh.refresh", icon:"https://cloud.wimzel.com/index.php/apps/files_sharing/ajax/publicpreview.php?x=1920&y=231&a=true&file=zone.png&t=LXsCzywKZxNtxpR&scalingup=0", backgroundColor:"#333333"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL", range:"(0..100)") {
				attributeState "level", action:"switch level.setLevel"
			}
		}

		standardTile("on", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"switch.on", icon:"st.Navien.bgs_power_off"
		}

		standardTile("off", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"switch.off", icon:"st.secondary.off"
		}

		valueTile("null", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:''
		}

		main "switch"
		
		details(["switch", "refresh", "on", "off"])
		
	}

}

// ========================================================
// PARSE - // parse events into attributes
// ========================================================

def parse(String description) {

	if (description == 'updated') return // don't poll when config settings is being updated as it may time out
	 
	poll()
 
}

// ========================================================
// SET LEVEL - // handle commands
// ========================================================

def setLevel(percentage) {

	if (percentage < 1 && percentage > 0) percentage = 1 // clamp to 1%

	if (percentage == 0) {
	
		sendEvent(name: "level", value: 0) // Otherwise the level value tile does not update
		
		return off() // if the brightness is set to 0, just turn it off
	
	}

	parent.logErrors() {
		if (parent.apiGET("/rflc_ctrl.cgi?_=1&cmd=rampz&zone=${selector()}&lvl=${percentage}") != null) {
			
			sendEvent(name: "level", value: percentage)
			sendEvent(name: "switch.setLevel", value: percentage)
		
		}
	}

}

// ========================================================
// ACTION - ON
// ========================================================

def on() {
	
	parent.logErrors() {
		if (parent.apiGET("/rflc_ctrl.cgi?_=1&cmd=rampz&zone=${selector()}&lvl=100") != null) {
			sendEvent(name: "switch", value: "on")
		}
	}
	
}

// ========================================================
// ACTION - OFF
// ========================================================

def off() {

	parent.logErrors() {
		if (parent.apiGET("/rflc_ctrl.cgi?_=1&cmd=rampz&zone=${selector()}&lvl=0") != null) {
			sendEvent(name: "switch", value: "off")
		}
	}

}

// ========================================================
// ACTION - POLL
// ========================================================

def poll() {
	
	log.debug "Executing 'poll' for ${device} ${this} ${selector()}"
	
	return []
	
}

// ========================================================
// ACTION - REFRESH
// ========================================================

def refresh() {
	
	log.debug "Executing 'refresh'"
	
	poll()
	
}

// ========================================================
// SELECTOR
// ========================================================

def selector() {
	
	if (device.deviceNetworkId.contains(":")) {
		return device.deviceNetworkId
	} else {
		return "${device.deviceNetworkId}"
	}
	
}