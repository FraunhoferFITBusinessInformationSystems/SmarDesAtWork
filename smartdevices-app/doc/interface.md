# Interface Definition

    TODO: This page is not finished yet.

## Configuration

````
{
  "deviceId": String(formatted: "[a-zA-Z0-9]*\.[a-zA-Z0-9]*",
  "deviceName": String [a-zA-Z0-9]*,
  "user": String [a-zA-Z0-9]*,
  "tabs": Array(Tab) [
    {
      "title": String,
      "key": String [],
      "mainTab": true,
      "showBadgeNumber": true,
      "icon": "job",
      "sortEntries": [
        {
          "name": "Text",
          "key": "text"
        },
      ],
      "filterEntries": [
        {
          "name": "Status",
          "key": "status",
          "invertable": true,
          "filterType": "state"
        }
      ]
    },
  ]
}
````

## Components



### Output Components

#### TextView
#### DateView
#### PictureView
#### ExternalUrl

### Input Components

#### Button
#### DoubleButton
#### Spinner
#### Switch
#### BarcodeInput
#### TextInput
#### DateInput
#### PictureInput
#### NumberInput

### Other Components
#### Spacer
#### GenericAction
#### JobListEntry
#### ToggleableEntry (TodoListEntry)
#### OptionDialog??

### Livedata Components
#### ValueDisplayPlain
#### ValueDisplayGauge
#### ValueDisplayAdvanced
#### ValueMonitor
#### ValueMonitorSingle


#### GraphDisplay

````
ValueProperties {
	"Id": String(UUID),
	"Type": String("ValueMessage"),
	"Name": String,
	"ValueName": String,
	"Value": Double,
	"SetPoint": Double,
	"HH": Double,
	"H": Double (Optional),
	"L": Double (Optional),
	"LL": Double,
	"Time" String(ISO8601-DateFormat) (Optional),
	"Severity": String("Normal"|"Warning"|"Error"|"Information") (Optional),
	"Unit": String("mm") (Optional)
}
````
