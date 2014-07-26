Geo Cluster Facet Plugin for elasticsearch
==========================================

This plugin provides a facet for [elasticsearch](http://www.elasticsearch.org/) to aggregate documents with `geo_point` fields.  
A naive, distance-based algorithm is used to build rectangular (and potentially overlapping) clusters with a weighted center.

To install the plugin, run:

```
bin/plugin --url https://github.com/zenobase/geocluster-facet/releases/download/0.0.11/geocluster-facet-0.0.11.jar --install geocluster-facet
```


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
			<td>0.0.11 -> master</td>
			<td>1.2.x</td>
		</tr>
		<tr>
			<td>0.0.10</td>
			<td>1.0.x, 1.1.x</td>
		</tr>
		<tr>
			<td>0.0.9</td>
			<td>0.90.6, 0.90.7</td>
		</tr>
		<tr>
			<td>0.0.8</td>
			<td>0.90.5</td>
		</tr>
		<tr>
			<td>0.0.7</td>
			<td>0.90.3</td>
		</tr>
		<tr>
			<td>0.0.6</td>
			<td>0.90.2</td>
		</tr>
		<tr>
			<td>0.0.5</td>
			<td>0.90.0, 0.90.1</td>
		</tr>
		<tr>
			<td>0.0.2 -> 0.0.4</td>
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
        	Defaults to 0.1. This value is relative to the size of the area that contains points, so it does not need to be adjusted e.g. when 
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

Copyright 2012-2014 Zenobase LLC

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
