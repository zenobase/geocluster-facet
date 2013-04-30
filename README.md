Geo Cluster Facet Plugin for elasticsearch
==========================================

This plugin provides a facet for [elasticsearch](http://www.elasticsearch.org/) to aggregate documents with `geo_point` fields.

To install the plugin, download and extract this plugin into the `plugins` dir.


Versions
--------

<table>
	<thead>
		<tr>
			<th>geocluster-facet</th>
			<th>elasticsearch</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>master</td>
			<td>0.20.x</td>
		</tr>
		<tr>
			<td><a href="https://docs.google.com/file/d/0B6V71rdiuHLJbnY1Q2xGQlVMUGc/edit?usp=sharing">0.0.3</a></td>
			<td>0.20.x</td>
		</tr>
		<tr>
			<td>0.0.2</td>
			<td>0.20.x</td>
		</tr>
		<tr>
            <td>0.0.1</td>
        	<td>0.19.x</td>
        </tr>
	</tbody>
</table>


Parameters
----------

<table>
	<tbody>
		<tr>
			<th>field</th>
			<td>The name of a field of type `geo_point`.</td>
		</tr>
		<tr>
            <th>factor</th>
        	<td>Controls the amount of clustering, from 0.0 (don't cluster any points) to 1.0 (create a single cluster containing all points). 
        	Defaults to 0.2. This value is relative to the size of the area that contains points, so it does not need to be adjusted e.g. when 
        	zooming in on a map.</td>
        </tr>
	</tbody>
</table>


Example
-------

Query:

```javascript
{
    "query" : { ... }
    "facets" : {
        "places" : { 
            "geo_cluster" : {
                "field" : "location",
                "factor" : 0.5
            }
        }
    }
}
```

```java
SearchSourceBuilder search = ...
search.facet(new GeoClusterFacetBuilder("places", "location", 0.5));
```

Result:

```javascript
{
    ...
    "facets" : {
        "geo_cluster" : [ {
        	"count" : 1,
        	"lat" : 36.08,
        	"lon" : -115.17
        }, {
            "count" : 3,
            "lat" : 39.75,
            "lon" : -104.87,
            "lat_min" : 37.00,
            "lat_max" : 41.00,
            "lon_min" : -109.05,
            "lon_max" : -102.04
        } ]
    }
}
```


License
-------

```
This software is licensed under the Apache 2 license, quoted below.

Copyright 2012-2013 Zenobase LLC

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
```
