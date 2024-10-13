local event = require "event"
local serial = require "serialization"
local component = require "component"
if ~component.isAvailable("internet") then
  print("This program requires an internet card to run.")
  os.exit()
end
local internet = component.internet
local sg = component.stargate
local conf = require "config.lua"

local eventQueue = {}
local con


local function getCon()
  local conGot;
  if conf.port ~= nil then
    conGot = internet.connect(conf.address, conf.port)
  else
    conGot = internet.connect(conf.address)
  end
  conGot.write(serial.serialize({
    os.date(),
    {
      "init",
      name = conf.name,
      status = sg.gateStatus(),
      dialed = sg.dialedAddress,
      hasDHD = component.isAvailable "dhd"
    }
  }))
  return conGot
end

local function sendEvent(e)
  eventQueue.insert(e)
  if con.write(serial.serialize(eventQueue)) > 0 then
    eventQueue = {}
  end
end

local function processEvent(e)
  if e[1]:sub(1, #"stargate") == "stargate" then
    return {
      os.date(),
      id = sg.address,
      type = "stargate",
      e
    }
  elseif e[1] == "modem_message" then
    return {
      os.date,
      id = sg.address,
      type = "modem",
      e
    }
  end
end

local function receiveCommand()
  local data = con.read()
  local command = {}
  if data ~= "" then
    command = serial.unserialize(data)
  end
  return command
end

local function execute(command)
  if command[1] == "update" then
    os.execute(
      "wget https://raw.githubusercontent.com/MR-Spagetty/JSG-remote-gate-logger/refs/heads/main/gateClient/client.lua /home/client.lua")
    os.execute(
      "wget https://raw.githubusercontent.com/MR-Spagetty/JSG-remote-gate-logger/refs/heads/main/gateClient/.shrc /home/.shrc")
    os.execute("reboot")
  elseif command[1] == "status" then
    con.write(serial.serialize { os.date(), id = sg.address,
      {
        "status",
        status = sg.gateStatus(),
        dialed = sg.dialedAddress,
        hasDHD = component.isAvailable "dhd"
      }
    })
  end
end

local function main()
  con = getCon()
  while true do
    local command = receiveCommand()
    if command ~= nil then
      execute(command)
      local event = { event.pull(0.1) }
      if event[1] then
        sendEvent(processEvent(event))
      end
    end
  end
end

main()
