<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="row mb-3">
    <div class="col-md-6">
        <div class="form-group">
            <label for="pickupAddress">Pickup Address</label>
            <div class="input-group">
                <input type="text" id="pickupAddress" name="pickupAddress" class="form-control" required>
                <button type="button" class="btn btn-outline-secondary" onclick="useCurrentLocationForPickup()">
                    <i class="fas fa-location-arrow"></i> Current
                </button>
            </div>
        </div>
    </div>
    <div class="col-md-6">
        <div class="form-group">
            <label for="deliveryAddress">Delivery Address</label>
            <div class="input-group">
                <input type="text" id="deliveryAddress" name="deliveryAddress" class="form-control" 
                       placeholder="Type delivery address or use current location" required>
                <button type="button" class="btn btn-outline-secondary" onclick="useCurrentLocationForDelivery()">
                    <i class="fas fa-location-arrow"></i> Current
                </button>
            </div>
        </div>
    </div>
</div>

<div id="map" style="height: 400px; width: 100%; margin: 20px 0;"></div>

<!-- Hidden inputs for coordinates -->
<input type="hidden" id="pickupLat" name="pickupLat">
<input type="hidden" id="pickupLng" name="pickupLng">
<input type="hidden" id="deliveryLat" name="deliveryLat">
<input type="hidden" id="deliveryLng" name="deliveryLng">

<script src="https://maps.googleapis.com/maps/api/js?key=${mapConfig.apiKey}&libraries=places"></script>
<script>
let map;
let markers = [];
let directionsService;
let directionsRenderer;
let pickupAutocomplete;
let deliveryAutocomplete;
let currentLocationMarker;

function initMap() {
    // Initialize the map
    map = new google.maps.Map(document.getElementById('map'), {
        zoom: ${mapConfig.zoom},
        center: { lat: ${mapConfig.latitude != null ? mapConfig.latitude : 0}, 
                 lng: ${mapConfig.longitude != null ? mapConfig.longitude : 0} }
    });

    // Initialize directions service
    directionsService = new google.maps.DirectionsService();
    directionsRenderer = new google.maps.DirectionsRenderer({
        map: map,
        suppressMarkers: true
    });

    // Initialize autocomplete for both addresses
    pickupAutocomplete = new google.maps.places.Autocomplete(
        document.getElementById('pickupAddress'),
        { types: ['address'] }
    );
    deliveryAutocomplete = new google.maps.places.Autocomplete(
        document.getElementById('deliveryAddress'),
        { types: ['address'] }
    );

    // Add listeners for place selection
    pickupAutocomplete.addListener('place_changed', function() {
        const place = pickupAutocomplete.getPlace();
        if (place.geometry) {
            document.getElementById('pickupLat').value = place.geometry.location.lat();
            document.getElementById('pickupLng').value = place.geometry.location.lng();
            updateRoute();
        }
    });

    deliveryAutocomplete.addListener('place_changed', function() {
        const place = deliveryAutocomplete.getPlace();
        if (place.geometry) {
            document.getElementById('deliveryLat').value = place.geometry.location.lat();
            document.getElementById('deliveryLng').value = place.geometry.location.lng();
            updateRoute();
        }
    });

    // Add input event listeners for manual address typing
    document.getElementById('deliveryAddress').addEventListener('input', function() {
        // Delay the geocoding to avoid too many API calls while typing
        clearTimeout(this.typingTimer);
        this.typingTimer = setTimeout(() => {
            const address = this.value;
            if (address.length > 3) { // Only geocode if address is longer than 3 characters
                geocodeAddress(address, 'delivery');
            }
        }, 1000); // Wait 1 second after typing stops
    });

    // Get current location on load
    getCurrentLocation();
}

function getCurrentLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            function(position) {
                const pos = {
                    lat: position.coords.latitude,
                    lng: position.coords.longitude
                };

                map.setCenter(pos);

                // Update current location marker
                if (currentLocationMarker) {
                    currentLocationMarker.setMap(null);
                }
                currentLocationMarker = new google.maps.Marker({
                    position: pos,
                    map: map,
                    icon: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png',
                    title: 'Current Location'
                });

                // Reverse geocode to get address
                const geocoder = new google.maps.Geocoder();
                geocoder.geocode({ location: pos }, function(results, status) {
                    if (status === 'OK' && results[0]) {
                        window.currentAddress = results[0].formatted_address;
                    }
                });
            },
            function() {
                console.log('Error: The Geolocation service failed.');
            }
        );
    }
}

function geocodeAddress(address, type) {
    const geocoder = new google.maps.Geocoder();
    geocoder.geocode({ address: address }, function(results, status) {
        if (status === 'OK' && results[0]) {
            const location = results[0].geometry.location;
            if (type === 'delivery') {
                document.getElementById('deliveryLat').value = location.lat();
                document.getElementById('deliveryLng').value = location.lng();
            }
            updateRoute();
        }
    });
}

function useCurrentLocationForPickup() {
    if (window.currentAddress) {
        document.getElementById('pickupAddress').value = window.currentAddress;
        if (currentLocationMarker) {
            document.getElementById('pickupLat').value = currentLocationMarker.getPosition().lat();
            document.getElementById('pickupLng').value = currentLocationMarker.getPosition().lng();
            updateRoute();
        }
    }
}

function useCurrentLocationForDelivery() {
    if (window.currentAddress) {
        document.getElementById('deliveryAddress').value = window.currentAddress;
        if (currentLocationMarker) {
            document.getElementById('deliveryLat').value = currentLocationMarker.getPosition().lat();
            document.getElementById('deliveryLng').value = currentLocationMarker.getPosition().lng();
            updateRoute();
        }
    }
}

function updateRoute() {
    // Clear existing markers
    markers.forEach(marker => marker.setMap(null));
    markers = [];

    const pickupAddress = document.getElementById('pickupAddress').value;
    const deliveryAddress = document.getElementById('deliveryAddress').value;

    if (pickupAddress && deliveryAddress) {
        const request = {
            origin: pickupAddress,
            destination: deliveryAddress,
            travelMode: 'DRIVING'
        };

        directionsService.route(request, function(result, status) {
            if (status === 'OK') {
                directionsRenderer.setDirections(result);
                
                // Add markers for pickup and delivery locations
                addMarker({
                    position: result.routes[0].legs[0].start_location,
                    title: 'Pickup Location',
                    icon: 'http://maps.google.com/mapfiles/ms/icons/green-dot.png'
                });
                
                addMarker({
                    position: result.routes[0].legs[0].end_location,
                    title: 'Delivery Location',
                    icon: 'http://maps.google.com/mapfiles/ms/icons/red-dot.png'
                });

                // Update hidden coordinate inputs
                document.getElementById('pickupLat').value = result.routes[0].legs[0].start_location.lat();
                document.getElementById('pickupLng').value = result.routes[0].legs[0].start_location.lng();
                document.getElementById('deliveryLat').value = result.routes[0].legs[0].end_location.lat();
                document.getElementById('deliveryLng').value = result.routes[0].legs[0].end_location.lng();
            }
        });
    }
}

function addMarker(markerConfig) {
    const marker = new google.maps.Marker({
        position: markerConfig.position,
        map: map,
        title: markerConfig.title,
        icon: markerConfig.icon
    });
    markers.push(marker);
}

// Initialize the map when the page loads
google.maps.event.addDomListener(window, 'load', initMap);
</script> 