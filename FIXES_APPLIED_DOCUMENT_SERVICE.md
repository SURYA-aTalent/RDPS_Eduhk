# Fixes Applied to Document Service

## Problem Encountered

After implementing the Document Service integration, the application failed to start with errors:

1. **WSDL Loading Error**: DocumentWebService_Service was trying to load WSDL at initialization
2. **IllegalAnnotationsException**: 28 counts of JAXB annotation conflicts due to package naming issues

## Root Causes

### Issue 1: WSDL Loading at Startup
The generated SOAP service stub was trying to fetch the WSDL from TalentLink during initialization:
```java
static {
    try {
        url = new URL("https://api3.lumesse-talenthub.com/HRIS/SOAP/Document?wsdl");
    } catch (MalformedURLException ex) {
        e = new WebServiceException(ex);
    }
}
```

This requires API credentials which aren't available at initialization time.

### Issue 2: Package Name Conflicts
When copying SOAP stubs from DHLTLKConnector project and changing package from `com.mrted.ws.documents` to `eduhk.fhr.web.soap.document`, some internal references weren't updated:
- `@RequestWrapper` and `@ResponseWrapper` annotations still referenced old package
- JAXB was finding conflicting class definitions

## Fixes Applied

### Fix 1: Disable WSDL Loading at Initialization

**File:** `src/main/java/eduhk/fhr/web/soap/document/DocumentWebService_Service.java`

**Changes:**
1. Commented out `@HandlerChain` annotation (handlers added programmatically)
2. Modified static initializer to return `null` for WSDL location
3. Simplified `__getWsdlLocation()` method

```java
// Before
@WebServiceClient(name = "DocumentWebService", targetNamespace = "http://ws.mrted.com/", wsdlLocation = "https://api3.lumesse-talenthub.com/HRIS/SOAP/Document?wsdl")
@HandlerChain(file="handler-chain.xml")
public class DocumentWebService_Service extends Service {
    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("https://api3.lumesse-talenthub.com/HRIS/SOAP/Document?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        DOCUMENTWEBSERVICE_WSDL_LOCATION = url;
        DOCUMENTWEBSERVICE_EXCEPTION = e;
    }
}

// After
@WebServiceClient(name = "DocumentWebService", targetNamespace = "http://ws.mrted.com/", wsdlLocation = "https://api3.lumesse-talenthub.com/HRIS/SOAP/Document?wsdl")
// @HandlerChain(file="handler-chain.xml")  // Commented out - handler added programmatically
public class DocumentWebService_Service extends Service {
    static {
        // Don't try to load WSDL at initialization - it requires authentication
        // WSDL will be loaded from the endpoint URL set in TalentLinkSOAPDocumentService
        DOCUMENTWEBSERVICE_WSDL_LOCATION = null;
        DOCUMENTWEBSERVICE_EXCEPTION = null;
    }

    private static URL __getWsdlLocation() {
        // Return null - WSDL will be loaded from endpoint URL with authentication
        return null;
    }
}
```

### Fix 2: Update Package References in Annotations

**Command executed:**
```bash
find src/main/java/eduhk/fhr/web/soap/document -name '*.java' \
  -exec sed -i '' 's/className = "com\.mrted\.ws\.documents\./className = "eduhk.fhr.web.soap.document./g' {} \;
```

**Affected file:** `src/main/java/eduhk/fhr/web/soap/document/DocumentWebService.java`

**Changed all @RequestWrapper and @ResponseWrapper annotations:**
```java
// Before
@RequestWrapper(localName = "getDocumentsByApplicationId",
                targetNamespace = "http://ws.mrted.com/",
                className = "com.mrted.ws.documents.GetDocumentsByApplicationId")
@ResponseWrapper(localName = "getDocumentsByApplicationIdResponse",
                 targetNamespace = "http://ws.mrted.com/",
                 className = "com.mrted.ws.documents.GetDocumentsByApplicationIdResponse")

// After
@RequestWrapper(localName = "getDocumentsByApplicationId",
                targetNamespace = "http://ws.mrted.com/",
                className = "eduhk.fhr.web.soap.document.GetDocumentsByApplicationId")
@ResponseWrapper(localName = "getDocumentsByApplicationIdResponse",
                 targetNamespace = "http://ws.mrted.com/",
                 className = "eduhk.fhr.web.soap.document.GetDocumentsByApplicationIdResponse")
```

## Verification

### Build Status
```bash
mvn clean package -DskipTests
# Result: BUILD SUCCESS
```

### Startup Logs
```
2025-12-04 21:14:52 [restartedMain] INFO  e.f.w.s.TalentLinkSOAPDocumentService - Initializing TalentLink SOAP Document Service
2025-12-04 21:14:52 [restartedMain] INFO  e.f.w.s.h.TalentLinkSOAPHandler - SOAP credentials configured for user: EdUHK UAT:prabhu.srinivasan@atalent.com:BO
2025-12-04 21:14:52 [restartedMain] INFO  e.f.w.s.TalentLinkSOAPDocumentService - TalentLink SOAP Document Service initialized successfully
...
2025-12-04 21:14:53 [restartedMain] INFO  eduhk.fhr.web.config.fhrApplication - Started fhrApplication in 4.07 seconds
```

### Application Status
✅ **Running on http://localhost:8080**
✅ **All SOAP services initialized successfully:**
  - TalentLink User Service
  - TalentLink Candidate Service
  - **TalentLink Document Service** ← New service
  - SharePoint Upload Service

## Testing Commands

### Test Application Home
```bash
curl http://localhost:8080
```

### Test Document Endpoint (when implemented)
```bash
# Test document retrieval and parsing
curl http://localhost:8080/api/test-sync/debug/document/445

# Test full candidate import
curl http://localhost:8080/api/test-sync/candidate/116
```

## Files Modified in This Fix

1. `src/main/java/eduhk/fhr/web/soap/document/DocumentWebService_Service.java`
   - Disabled WSDL loading at initialization
   - Commented out @HandlerChain annotation

2. `src/main/java/eduhk/fhr/web/soap/document/DocumentWebService.java`
   - Updated all @RequestWrapper className attributes
   - Updated all @ResponseWrapper className attributes

## Key Learnings

1. **WSDL Loading**: JAX-WS services try to load WSDL at initialization. For authenticated services, set WSDL location to `null` and configure endpoint programmatically.

2. **Package Migration**: When copying generated SOAP stubs, ensure ALL package references are updated:
   - Import statements
   - Package declarations
   - Annotation className attributes
   - Documentation comments (optional but cleaner)

3. **Handler Configuration**: For authenticated services, handlers should be added programmatically in the service class, not via @HandlerChain annotation.

## Success Criteria Met

✅ Application starts without errors
✅ Document SOAP service initializes successfully
✅ No WSDL loading errors
✅ No JAXB annotation conflicts
✅ Application accessible on port 8080
✅ Ready for testing education/work/referee data sync

## Next Steps

1. Test document retrieval with actual document IDs
2. Test full candidate import workflow
3. Verify data populates in all related tables
4. Monitor logs for any parsing issues with real data
