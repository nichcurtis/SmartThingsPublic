/**
 *  22u Home Automation
 *
 *  Copyright 2016 Nicholas Curtis
 *
 */
definition(
    name: "22u Home Automation",
    namespace: "22u",
    author: "Nicholas Curtis",
    description: "22u Home Automation system integration with Smart Things hub.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: [displayName: "22u Home Automation", displayLink: "http://home.22u.io"])


preferences {
  section ("Allow 22u to control: ") {
    input "switches", "capability.switch", title: "Lights & Switches", multiple: true, required: true
  }
}

mappings {
    path("/control/switch") {
        action: [
			GET: "getAll"
		]
    }
    path("/control/switch/:id") {
        action: [
        	GET: "get",
            PUT: "put"
		]
    }
}

def put() {
	return putSwitch(switches, params.id, request.JSON?.state);
}

def get() {
	return getSwitch(switches, params.id);
}

def getAll() {
    return getSwitches(switches)
}

def putSwitch(allSwitches, switchId, request) {
	def thisSwitch = allSwitches.find {
    	return it.id == switchId
    }
    
    if (request.power) {
    	if (request.brightness != 'none') {
            log.debug "set switch ${switchId} level to ${request.brightness}%"
            thisSwitch.setLevel(request.brightness)
    	}
	    else {
        	log.debug "turning switch ${switchId} on"
            thisSwitch.on()
        }
    } else {
    	log.debug "turning switch ${switchId} off"
        thisSwitch.off()
	}
}

def getSwitch(allSwitches, switchId) {
	return getSwitches(allSwitches).find {
    	return it.id == switchId
    }
}

def getSwitches(allSwitches) {
	def resp = []
    allSwitches.each {
    	log.debug "getSwitches: ${it.supportedAttributes}"
        resp << [
        	id: it.id,
            name: it.displayName,
            type: it.type,
            features: [
        		power: it.currentValue("switch"),
	            brightness: it.currentValue('level') != null ? it.currentValue('level') : 'none',
    	        color: 'none',
                status: it.currentValue('indicatorStatus') != null ? it.currentValue('indicatorStatus') : 'none',
        	],
            events: it.events()
		]
    }
    return resp
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
}