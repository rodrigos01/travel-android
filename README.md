# Travel Android App

## Security Configuration

To run and build this application locally, you need to configure the required secrets. Create a `local.properties` file in the root directory and add the following keys. 

```properties
# Google Maps API Key
MAPS_API_KEY=your_maps_api_key_here

# Backend Service Secret
CLIENT_SECRET=your_backend_client_secret_here

# Release Keystore Configuration (for making release builds)
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=your_key_alias
KEY_PASSWORD=your_key_password
```

**Note**: For CI/CD environments (like Cloud Build), these variables can alternatively be provided as environment variables instead of through `local.properties`.

Ensure you also place a valid `google-services.json` inside the `app/` directory for Firebase services to work correctly.
