# SharePoint Upload Integration - Technical Documentation

**Project:** RDPS (Recruitment Data Processing System)
**Feature:** Manual File Upload to SharePoint via MS Graph API
**Version:** 1.0
**Date:** November 25, 2025
**Author:** Development Team

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Feature Overview](#feature-overview)
3. [Architecture & Design](#architecture--design)
4. [Prerequisites](#prerequisites)
5. [Azure AD Configuration](#azure-ad-configuration)
6. [Application Configuration](#application-configuration)
7. [Technical Implementation](#technical-implementation)
8. [User Guide](#user-guide)
9. [Security Considerations](#security-considerations)
10. [Troubleshooting](#troubleshooting)
11. [API Reference](#api-reference)
12. [Future Enhancements](#future-enhancements)

---

## Executive Summary

This document describes the implementation of a SharePoint file upload feature in the RDPS application. The feature allows authenticated users to manually upload files from their local machine directly to SharePoint Online using Microsoft Graph API.

### Key Features
- **Manual file upload** with drag-and-drop interface
- **Direct integration** with SharePoint via MS Graph API
- **Client Credentials authentication** (app-only access)
- **File validation** (type and size restrictions)
- **Real-time upload feedback** with web URLs
- **No database tracking** (simplified implementation)

### Technology Stack
- **Backend:** Spring Boot 4.0.0-SNAPSHOT (Java 21)
- **Authentication:** Azure Identity SDK (Client Credentials flow)
- **API:** Microsoft Graph SDK v5.80.0
- **Frontend:** Dropzone.js, Thymeleaf, jQuery
- **Security:** Spring Security with role-based access

---

## Feature Overview

### What This Feature Does

The SharePoint upload feature enables users with the `ROLE_USE_RDPS` permission to:

1. Navigate to a dedicated upload page (`/sharepoint/upload`)
2. Select or drag-and-drop files for upload
3. Automatically upload files to a configured SharePoint site
4. Receive SharePoint web URLs for uploaded files
5. Validate file types and sizes before upload

### What This Feature Does NOT Do

- **Database tracking:** Files are not recorded in RDPS database
- **Metadata storage:** No custom metadata or categorization
- **Candidate linking:** Files are not associated with candidate records
- **Version control:** No versioning beyond SharePoint's default behavior
- **Approval workflow:** Files are uploaded immediately without approval

### Use Cases

1. **Manual document submission:** HR staff uploading candidate documents
2. **Ad-hoc file sharing:** Quick uploads for immediate SharePoint access
3. **Backup uploads:** Manual backup of important files
4. **Temporary storage:** Files needed for short-term collaboration

---

## Architecture & Design

### System Architecture

```
┌─────────────────┐
│   Web Browser   │
│   (Dropzone.js) │
└────────┬────────┘
         │ HTTP POST /sharepoint/ajax/upload
         │ (multipart/form-data)
         ▼
┌─────────────────────────────────────────┐
│   Spring Boot Application               │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │ SharePointUploadController        │ │
│  │ - Handles HTTP requests           │ │
│  │ - CSRF validation                 │ │
│  │ - Role-based authorization        │ │
│  └──────────────┬────────────────────┘ │
│                 │                        │
│  ┌──────────────▼────────────────────┐ │
│  │ SharePointUploadService           │ │
│  │ - File validation (type, size)    │ │
│  │ - Filename sanitization           │ │
│  │ - MS Graph API calls              │ │
│  └──────────────┬────────────────────┘ │
│                 │                        │
│  ┌──────────────▼────────────────────┐ │
│  │ GraphServiceClient                │ │
│  │ - Azure authentication            │ │
│  │ - HTTP requests to Graph API      │ │
│  └──────────────┬────────────────────┘ │
└─────────────────┼────────────────────────┘
                  │ HTTPS
                  │ Authorization: Bearer {token}
                  ▼
         ┌────────────────────┐
         │  Microsoft Graph   │
         │       API          │
         └─────────┬──────────┘
                   │
                   ▼
         ┌────────────────────┐
         │  SharePoint Online │
         │   Document Library │
         └────────────────────┘
```

### Component Breakdown

#### 1. **SharePointConfig.java**
- **Purpose:** Configuration management
- **Pattern:** Spring `@Configuration` with `@PropertySource`
- **Properties loaded:**
  - Tenant ID, Client ID, Client Secret
  - Site ID, Drive ID
  - Max file size (MB)
  - Allowed file extensions

#### 2. **SharePointUploadService.java**
- **Purpose:** Business logic and MS Graph integration
- **Pattern:** Spring `@Service` extending `BaseService`
- **Initialization:** `@PostConstruct` method (like TalentLink services)
- **Key methods:**
  - `init()` - Initialize GraphServiceClient with credentials
  - `uploadFile(MultipartFile)` - Upload file to SharePoint
  - `validateFile(MultipartFile)` - Validate file type and size
  - `sanitizeFilename(String)` - Remove unsafe characters

#### 3. **SharePointUploadController.java**
- **Purpose:** HTTP request handling
- **Pattern:** Spring `@Controller` extending `BaseController`
- **Endpoints:**
  - `GET /sharepoint/upload` - Display upload page
  - `POST /sharepoint/ajax/upload` - Handle file upload (AJAX)
- **Security:** `@PreAuthorize("hasRole('USE_RDPS')")`

#### 4. **upload.html (Thymeleaf Template)**
- **Purpose:** User interface
- **Components:**
  - Dropzone.js for drag-and-drop
  - CSRF token handling
  - Upload progress feedback
  - Success/error notifications (toastr)

### Design Decisions

#### Why MS Graph SDK v5.80.0?
- **Compatibility:** v6 has breaking API changes
- **Stability:** v5 is mature and well-documented
- **Dependencies:** Works seamlessly with Azure Identity SDK v1.11.4

#### Why Client Credentials Flow?
- **App-only access:** No user interaction required
- **Service accounts:** Backend service authentication
- **Consistent permissions:** Same access for all users

#### Why No Database Tracking?
- **Simplification:** User requested minimal implementation
- **SharePoint as source of truth:** Files managed in SharePoint
- **Reduced coupling:** No dependency on RDPS database schema

#### Why @PostConstruct Initialization?
- **Consistency:** Follows existing pattern (TalentLinkSOAPCandidateService)
- **Fail-fast:** Errors detected at startup, not runtime
- **Single initialization:** GraphServiceClient created once

---

## Prerequisites

### Required Software
1. **Java 21** - Application compiled with class version 61.0
2. **Maven 3.8+** - Dependency management and build
3. **Azure AD tenant** - For application registration
4. **SharePoint Online** - Document library for uploads

### Required Access
1. **Azure AD Global Administrator** - To register applications
2. **SharePoint Administrator** - To grant site permissions
3. **Application Developer** - To configure RDPS application

### Development Environment
```bash
# Verify Java version
java -version
# Expected: openjdk version "21.x.x"

# Verify Maven version
mvn -version
# Expected: Apache Maven 3.8.x or higher
```

---

## Azure AD Configuration

### Step 1: Register Application in Azure AD

1. **Navigate to Azure Portal**
   - URL: https://portal.azure.com
   - Go to: **Azure Active Directory** > **App registrations**

2. **Create New Registration**
   - Click: **New registration**
   - **Name:** `RDPS SharePoint Integration`
   - **Supported account types:** Single tenant
   - **Redirect URI:** Not required (app-only auth)
   - Click: **Register**

3. **Record Application Details**
   ```
   Application (client) ID: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
   Directory (tenant) ID:   xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
   ```

### Step 2: Create Client Secret

1. **Navigate to Certificates & secrets**
   - Left menu: **Certificates & secrets**
   - Click: **New client secret**
   - **Description:** `RDPS Production Secret`
   - **Expires:** 24 months (recommended)
   - Click: **Add**

2. **Copy Secret Value IMMEDIATELY**
   ```
   Client Secret Value: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   ```
   ⚠️ **WARNING:** Secret value shown only once. Store securely.

### Step 3: Grant API Permissions

1. **Navigate to API permissions**
   - Left menu: **API permissions**
   - Click: **Add a permission**
   - Select: **Microsoft Graph** > **Application permissions**

2. **Add Required Permissions**
   - **Files.ReadWrite.All** - Read and write files in all site collections
   - **Sites.ReadWrite.All** - Read and write items in all site collections
   - Click: **Add permissions**

3. **Grant Admin Consent**
   - Click: **Grant admin consent for [Your Tenant]**
   - Confirm: **Yes**
   - Status should show: ✅ Granted for [Your Tenant]

### Step 4: Get SharePoint Site and Drive IDs

#### Method 1: Using Microsoft Graph Explorer (Recommended)

1. **Navigate to Graph Explorer**
   - URL: https://developer.microsoft.com/en-us/graph/graph-explorer

2. **Get Site ID**
   - Request: `GET https://graph.microsoft.com/v1.0/sites?search=YOUR_SITE_NAME`
   - Copy `id` value from response
   - Format: `contoso.sharepoint.com,xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx,xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`

3. **Get Drive ID**
   - Request: `GET https://graph.microsoft.com/v1.0/sites/{site-id}/drives`
   - Copy `id` value for target document library
   - Format: `b!xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

#### Method 2: Using PowerShell

```powershell
# Install PnP PowerShell module
Install-Module -Name PnP.PowerShell

# Connect to SharePoint
Connect-PnPOnline -Url "https://yourtenant.sharepoint.com/sites/yoursite" -Interactive

# Get Site ID
$site = Get-PnPSite -Includes Id
$site.Id

# Get Drive ID (Document Library)
$list = Get-PnPList -Identity "Documents"
$list.Id
```

#### Method 3: Using SharePoint URL

If your SharePoint site is: `https://contoso.sharepoint.com/sites/HRDocuments`

**To get Site ID:**
- API: `GET https://graph.microsoft.com/v1.0/sites/contoso.sharepoint.com:/sites/HRDocuments`
- Copy `id` from response

**To get Drive ID:**
- API: `GET https://graph.microsoft.com/v1.0/sites/{site-id}/drives`
- Find drive with `name: "Documents"` (or your library name)
- Copy `id` value

---

## Application Configuration

### Configuration File Location
```
src/main/resources/application-local.properties
```

### Required Configuration Properties

```properties
# SharePoint Configuration
# Azure AD Application Registration Details
sharepoint.tenant.id=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
sharepoint.client.id=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
sharepoint.client.secret=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# SharePoint Site and Drive IDs
sharepoint.site.id=contoso.sharepoint.com,xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx,xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
sharepoint.drive.id=b!xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# Upload Settings
sharepoint.max.file.size.mb=50
sharepoint.allowed.extensions=pdf,doc,docx,jpg,jpeg,png,xls,xlsx,txt
```

### Configuration Parameters Explained

| Parameter | Description | Example | Required |
|-----------|-------------|---------|----------|
| `tenant.id` | Azure AD Tenant (Directory) ID | `72f988bf-86f1-41af-91ab-...` | Yes |
| `client.id` | Application (Client) ID | `a1b2c3d4-5678-90ab-cdef-...` | Yes |
| `client.secret` | Client Secret Value | `abC123~xYz789...` | Yes |
| `site.id` | SharePoint Site ID (full format) | `contoso.sharepoint.com,guid1,guid2` | Yes |
| `drive.id` | Document Library Drive ID | `b!aBcD1234...` | Yes |
| `max.file.size.mb` | Maximum file size in MB | `50` | Yes |
| `allowed.extensions` | Comma-separated file types | `pdf,doc,docx,jpg,png` | Yes |

### Security Best Practices

1. **Never commit secrets to version control**
   ```bash
   # Add to .gitignore
   echo "src/main/resources/application-local.properties" >> .gitignore
   ```

2. **Use environment-specific properties**
   - `application-local.properties` - Development
   - `application-production.properties` - Production (not committed)

3. **Rotate secrets regularly**
   - Create new client secret every 12-24 months
   - Update configuration before old secret expires

4. **Restrict file types**
   - Only allow necessary file extensions
   - Block executable files (.exe, .bat, .sh, .ps1)

5. **Limit file size**
   - Prevent large file uploads (default: 50 MB)
   - Consider SharePoint storage quotas

---

## Technical Implementation

### Files Created/Modified

#### New Files (4)

1. **src/main/java/eduhk/fhr/web/config/SharePointConfig.java**
   - Lines: 43
   - Purpose: Load SharePoint configuration from properties file

2. **src/main/java/eduhk/fhr/web/service/SharePointUploadService.java**
   - Lines: 171
   - Purpose: Core service for MS Graph API integration

3. **src/main/java/eduhk/fhr/web/controller/SharePointUploadController.java**
   - Lines: 78
   - Purpose: HTTP endpoint for file uploads

4. **src/main/resources/templates/sharepoint/upload.html**
   - Lines: 96
   - Purpose: User interface with Dropzone.js

#### Modified Files (4)

1. **pom.xml**
   - Lines modified: 229-248
   - Changes: Added MS Graph SDK dependencies

2. **src/main/resources/application-local.properties**
   - Lines added: 18-29
   - Changes: Added SharePoint configuration properties

3. **src/main/java/eduhk/fhr/web/config/WebSecurityConfig.java**
   - Line 50: Added security rule for `/sharepoint/**`
   - Changes: Role-based authorization

4. **src/main/resources/templates/fragments/nav.html**
   - Line 18: Added menu item
   - Changes: Navigation link to upload page

### Maven Dependencies Added

```xml
<!-- Microsoft Graph SDK for SharePoint Integration -->
<dependency>
    <groupId>com.microsoft.graph</groupId>
    <artifactId>microsoft-graph</artifactId>
    <version>5.80.0</version>
</dependency>

<!-- Azure Identity (for authentication) -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.11.4</version>
</dependency>

<!-- Azure HTTP client -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-http-netty</artifactId>
    <version>1.13.11</version>
</dependency>
```

### Code Walkthrough

#### 1. Service Initialization (@PostConstruct)

```java
@PostConstruct
public void init() {
    try {
        logger.info("Initializing SharePoint Upload Service");

        // Create credential using Client Credentials flow
        TokenCredential credential = new ClientSecretCredentialBuilder()
            .tenantId(config.getTenantId())
            .clientId(config.getClientId())
            .clientSecret(config.getClientSecret())
            .build();

        // Create scopes for authentication
        List<String> scopes = Collections.singletonList(
            "https://graph.microsoft.com/.default"
        );

        // Create authentication provider
        TokenCredentialAuthProvider authProvider =
            new TokenCredentialAuthProvider(scopes, credential);

        // Build Graph client with authentication provider
        graphClient = GraphServiceClient.builder()
            .authenticationProvider(authProvider)
            .buildClient();

        initialized = true;
        logger.info("SharePoint Upload Service initialized successfully");

    } catch (Exception e) {
        logger.error("Failed to initialize SharePoint Upload Service: {}",
            e.getMessage(), e);
        throw new RuntimeException("Failed to initialize SharePoint service", e);
    }
}
```

**Key Points:**
- Runs automatically after bean construction
- Creates Azure credential with tenant/client ID and secret
- Builds GraphServiceClient once (reused for all uploads)
- Throws exception on failure (fail-fast approach)

#### 2. File Upload Method

```java
public DriveItem uploadFile(MultipartFile file) throws Exception {
    if (!initialized) {
        throw new IllegalStateException("SharePoint service not initialized");
    }

    // Validate file (type and size)
    validateFile(file);

    // Sanitize filename (remove unsafe characters)
    String sanitizedFilename = sanitizeFilename(file.getOriginalFilename());

    logger.info("Uploading file: {} (size: {} bytes)",
        sanitizedFilename, file.getSize());

    // Read file bytes
    byte[] fileBytes = file.getInputStream().readAllBytes();

    // Upload to SharePoint root
    DriveItem uploadedItem = graphClient
        .sites(config.getSiteId())
        .drives(config.getDriveId())
        .root()
        .itemWithPath(sanitizedFilename)
        .content()
        .buildRequest()
        .put(fileBytes);

    logger.info("File uploaded successfully. SharePoint ID: {}",
        uploadedItem.id);
    logger.info("Web URL: {}", uploadedItem.webUrl);

    return uploadedItem;
}
```

**Key Points:**
- Checks initialization status before proceeding
- Validates file type and size
- Sanitizes filename to prevent path traversal
- Uploads to drive root (can be modified for subfolders)
- Returns DriveItem with SharePoint metadata

#### 3. File Validation

```java
private void validateFile(MultipartFile file) throws Exception {
    if (file.isEmpty()) {
        throw new Exception("File is empty");
    }

    // Check file size
    long maxSizeBytes = config.getMaxFileSizeMb() * 1024L * 1024L;
    if (file.getSize() > maxSizeBytes) {
        throw new Exception("File size exceeds maximum: " +
            config.getMaxFileSizeMb() + " MB");
    }

    // Check file extension
    String filename = file.getOriginalFilename();
    if (filename == null || !filename.contains(".")) {
        throw new Exception("File must have an extension");
    }

    String extension = filename.substring(
        filename.lastIndexOf(".") + 1
    ).toLowerCase();

    List<String> allowedList = Arrays.asList(
        config.getAllowedExtensions().split(",")
    );

    if (!allowedList.contains(extension)) {
        throw new Exception("File type not allowed. Allowed: " +
            config.getAllowedExtensions());
    }
}
```

**Validation Rules:**
- File must not be empty
- File size must be ≤ configured maximum
- File must have an extension
- Extension must be in allowed list

#### 4. Filename Sanitization

```java
private String sanitizeFilename(String filename) {
    if (filename == null) {
        return "unnamed_file";
    }

    // Remove unsafe characters: \ / : * ? " < > |
    String sanitized = filename.replaceAll("[\\\\/:*?\"<>|]", "_");

    // Limit length to 200 characters
    if (sanitized.length() > 200) {
        String extension = "";
        int lastDot = sanitized.lastIndexOf('.');
        if (lastDot > 0) {
            extension = sanitized.substring(lastDot);
            sanitized = sanitized.substring(0, 200 - extension.length())
                + extension;
        } else {
            sanitized = sanitized.substring(0, 200);
        }
    }

    return sanitized;
}
```

**Security Features:**
- Removes Windows/SharePoint forbidden characters
- Prevents directory traversal attacks
- Limits filename length to 200 characters
- Preserves file extension

#### 5. Controller Endpoint

```java
@PostMapping("/ajax/upload")
@ResponseBody
public AjaxResponse uploadFile(@RequestParam("file") MultipartFile file) {
    try {
        // Check service initialization
        if (!uploadService.isInitialized()) {
            return AjaxResponse.error(
                "SharePoint service not initialized. Check configuration."
            );
        }

        // Upload file
        DriveItem driveItem = uploadService.uploadFile(file);

        // Build response
        Map<String, Object> result = new HashMap<>();
        result.put("fileId", driveItem.id);
        result.put("fileName", driveItem.name);
        result.put("webUrl", driveItem.webUrl);
        result.put("size", driveItem.size);

        return AjaxResponse.success(result);

    } catch (Exception e) {
        logger.error("Error uploading file: {}", e.getMessage(), e);
        return AjaxResponse.error("Upload failed: " + e.getMessage());
    }
}
```

**Response Format:**
```json
{
  "success": true,
  "result": {
    "fileId": "01ABCDEF1234567890",
    "fileName": "document.pdf",
    "webUrl": "https://contoso.sharepoint.com/sites/...",
    "size": 1048576
  }
}
```

#### 6. Frontend (Dropzone.js)

```javascript
const myDropzone = new Dropzone("#myDropzone", {
    url: "/sharepoint/ajax/upload",
    paramName: "file",
    maxFilesize: 50, // MB
    acceptedFiles: ".pdf,.doc,.docx,.jpg,.jpeg,.png,.xls,.xlsx,.txt",
    addRemoveLinks: true,

    init: function() {
        // Add CSRF token to all requests
        this.on("sending", function(file, xhr, formData) {
            formData.append(csrfName, csrfValue);
        });

        // Handle successful upload
        this.on("success", function(file, response) {
            if (response.success) {
                toastr.success('File uploaded: ' + response.result.fileName);

                // Display SharePoint link
                $('#uploadedFilesList').append(
                    '<li class="list-group-item">' +
                    '<strong>' + response.result.fileName + '</strong><br>' +
                    '<small>SharePoint URL: <a href="' +
                    response.result.webUrl + '" target="_blank">' +
                    response.result.webUrl + '</a></small>' +
                    '</li>'
                );
            } else {
                toastr.error(response.message || 'Upload failed');
            }
            this.removeFile(file);
        });

        // Handle errors
        this.on("error", function(file, errorMessage) {
            toastr.error('Upload error: ' + errorMessage);
            this.removeFile(file);
        });
    }
});
```

---

## User Guide

### Accessing the Upload Feature

#### Step 1: Login to RDPS
- URL: `http://localhost:8080/RDPS` (development)
- Credentials: User with `ROLE_USE_RDPS` permission
- Default dev user: `fhr-dev-uacct01`

#### Step 2: Navigate to Upload Page
- **Method 1:** Click menu item "Upload to SharePoint"
- **Method 2:** Direct URL: `/sharepoint/upload`

#### Step 3: Upload Files

**Option A: Drag and Drop**
1. Drag files from file explorer
2. Drop onto the upload zone (blue dashed border)
3. Files upload automatically

**Option B: Click to Browse**
1. Click anywhere in the upload zone
2. Select files from file picker dialog
3. Click "Open"
4. Files upload automatically

### Upload Process

1. **File Validation**
   - System checks file type and size
   - Invalid files show error message
   - Valid files proceed to upload

2. **Upload Progress**
   - Progress bar shows upload status
   - File name and size displayed
   - Cancel option available during upload

3. **Upload Completion**
   - Success notification displayed (green toast)
   - SharePoint web URL shown
   - Click URL to open in SharePoint

4. **Error Handling**
   - Error notification displayed (red toast)
   - Error message explains issue
   - File can be re-uploaded

### Allowed File Types

By default, the following file types are allowed:
- **Documents:** .pdf, .doc, .docx, .txt
- **Spreadsheets:** .xls, .xlsx
- **Images:** .jpg, .jpeg, .png

### File Size Limit

- **Maximum size:** 50 MB (configurable)
- Files exceeding limit show error message

### Multiple File Upload

- **Supported:** Yes
- **Limit:** No hard limit (browser memory dependent)
- **Behavior:** Files upload sequentially

### SharePoint Location

Uploaded files are stored in:
```
SharePoint Site
  └── Document Library (configured drive)
       └── Root folder
            └── [uploaded files]
```

---

## Security Considerations

### Authentication & Authorization

#### Application-Level Security
- **Pre-authentication:** OAM_REMOTE_USER header validation
- **Role requirement:** `ROLE_USE_RDPS` mandatory
- **Session management:** Spring Security session handling
- **CSRF protection:** Token validation on all POST requests

#### Azure AD Security
- **App-only authentication:** No user credentials exposed
- **Client secret rotation:** Required every 12-24 months
- **Permission scope:** Limited to Files.ReadWrite.All and Sites.ReadWrite.All
- **Audit logging:** Azure AD tracks all API calls

### Input Validation

#### Filename Sanitization
```java
// Removed characters: \ / : * ? " < > |
String sanitized = filename.replaceAll("[\\\\/:*?\"<>|]", "_");
```

**Prevents:**
- Directory traversal attacks (`../../../etc/passwd`)
- Path injection (`folder/../../../sensitive.txt`)
- Invalid SharePoint characters

#### File Type Validation
- Extension whitelist (not blacklist)
- Case-insensitive matching
- Configurable allowed types

#### File Size Validation
- Server-side enforcement (not just client-side)
- Prevents denial-of-service via large uploads
- Configurable maximum size

### Network Security

#### HTTPS Requirements
- **Production:** HTTPS mandatory for MS Graph API
- **Client secret:** Transmitted over TLS only
- **Access tokens:** Bearer tokens in Authorization header

#### Firewall Rules
Required outbound access:
- `https://graph.microsoft.com` - MS Graph API
- `https://login.microsoftonline.com` - Azure AD authentication
- `https://*.sharepoint.com` - SharePoint Online

### Data Privacy

#### No Sensitive Data Logging
```java
// ✅ Good: Log filename only
logger.info("Uploading file: {}", sanitizedFilename);

// ❌ Bad: Log file contents
// logger.info("File contents: {}", fileBytes);
```

#### GDPR Compliance
- Files uploaded to SharePoint follow Microsoft's GDPR compliance
- No personal data stored in RDPS database
- Audit trail maintained in Azure AD logs

### Secrets Management

#### Current Implementation
- Secrets stored in `application-local.properties`
- **WARNING:** Not suitable for production

#### Production Recommendations

**Option 1: Environment Variables**
```properties
sharepoint.client.secret=${SHAREPOINT_CLIENT_SECRET}
```
```bash
export SHAREPOINT_CLIENT_SECRET="your-secret-here"
mvn spring-boot:run
```

**Option 2: Azure Key Vault**
```xml
<dependency>
    <groupId>com.azure.spring</groupId>
    <artifactId>spring-cloud-azure-starter-keyvault-secrets</artifactId>
</dependency>
```
```properties
spring.cloud.azure.keyvault.secret.endpoint=https://your-vault.vault.azure.net/
sharepoint.client.secret=${sharepoint-client-secret}
```

**Option 3: Spring Cloud Config Server**
```properties
spring.config.import=optional:configserver:http://config-server:8888
```

### Vulnerability Mitigation

| Vulnerability | Mitigation |
|---------------|------------|
| **Path Traversal** | Filename sanitization removes `..` and `/` |
| **XSS** | Thymeleaf auto-escapes output, CSP headers |
| **CSRF** | Spring Security CSRF tokens on all POST |
| **File Upload DoS** | Size limits, type validation |
| **Credential Exposure** | Secrets not logged, HTTPS only |
| **Unauthorized Access** | Role-based authorization required |

---

## Troubleshooting

### Common Issues and Solutions

#### Issue 1: "SharePoint service not initialized"

**Symptoms:**
- Error message on upload page
- Application logs show initialization failure

**Causes:**
1. Missing or invalid configuration properties
2. Incorrect Azure AD credentials
3. Network connectivity issues

**Solutions:**

**Check Configuration:**
```bash
# Verify all properties are set
grep "sharepoint\." src/main/resources/application-local.properties
```

**Check Logs:**
```bash
tail -100 catalina.base_IS_UNDEFINED/logs/server.log_RDPS.log | grep SharePoint
```

**Expected log output:**
```
INFO  - Initializing SharePoint Upload Service
INFO  - SharePoint Upload Service initialized successfully
```

**Error log output:**
```
ERROR - Failed to initialize SharePoint Upload Service:
        AADSTS7000215: Invalid client secret provided
```

**Fix:**
1. Verify client secret in Azure AD
2. Update `sharepoint.client.secret` in properties
3. Restart application

---

#### Issue 2: "AADSTS7000215: Invalid client secret provided"

**Symptoms:**
- Initialization fails
- Authentication error in logs

**Causes:**
- Client secret expired
- Client secret typo in configuration
- Wrong client secret copied

**Solutions:**

1. **Create New Client Secret:**
   - Azure Portal → App registrations → Your app
   - Certificates & secrets → New client secret
   - Copy new secret value

2. **Update Configuration:**
   ```properties
   sharepoint.client.secret=NEW_SECRET_HERE
   ```

3. **Restart Application:**
   ```bash
   mvn spring-boot:run
   ```

---

#### Issue 3: "Insufficient privileges to complete the operation"

**Symptoms:**
- Upload fails
- Error: "Access denied" or "Insufficient privileges"

**Causes:**
- Missing API permissions in Azure AD
- Admin consent not granted

**Solutions:**

1. **Check API Permissions:**
   - Azure Portal → App registrations → Your app
   - API permissions
   - Verify: Files.ReadWrite.All, Sites.ReadWrite.All

2. **Grant Admin Consent:**
   - Click "Grant admin consent for [Tenant]"
   - Confirm: Yes
   - Wait 5 minutes for propagation

3. **Verify Permission Status:**
   - All permissions should show: ✅ Granted

---

#### Issue 4: "File type not allowed"

**Symptoms:**
- Upload rejected
- Error message: "File type not allowed. Allowed: pdf,doc,docx,..."

**Causes:**
- File extension not in whitelist
- Case sensitivity issue

**Solutions:**

**Option 1: Add File Type to Whitelist**
```properties
# Add .zip to allowed extensions
sharepoint.allowed.extensions=pdf,doc,docx,jpg,jpeg,png,xls,xlsx,txt,zip
```

**Option 2: Verify File Extension**
- Check actual file extension (not just name)
- Windows: File Properties → Type
- macOS: Get Info → Kind

**Restart Application:**
```bash
mvn spring-boot:run
```

---

#### Issue 5: "File size exceeds maximum"

**Symptoms:**
- Large file upload rejected
- Error: "File size exceeds maximum: 50 MB"

**Causes:**
- File larger than configured limit
- Default limit too small

**Solutions:**

**Increase Size Limit:**
```properties
# Increase to 100 MB
sharepoint.max.file.size.mb=100
```

**Also Update Dropzone.js:**
```javascript
// In upload.html
maxFilesize: 100, // Match backend limit
```

**Restart Application**

**Note:** SharePoint has its own limits:
- Small files (< 250 MB): Use PUT method (current implementation)
- Large files (> 250 MB): Requires upload session (not implemented)

---

#### Issue 6: "ItemNotFound: The resource could not be found"

**Symptoms:**
- Upload fails
- Error: "ItemNotFound" or "404 Not Found"

**Causes:**
- Incorrect Site ID or Drive ID
- Site/drive deleted or moved
- Permission issues

**Solutions:**

1. **Verify Site ID:**
   ```bash
   curl -X GET \
     "https://graph.microsoft.com/v1.0/sites/{site-id}" \
     -H "Authorization: Bearer {token}"
   ```

2. **Verify Drive ID:**
   ```bash
   curl -X GET \
     "https://graph.microsoft.com/v1.0/sites/{site-id}/drives" \
     -H "Authorization: Bearer {token}"
   ```

3. **Re-obtain IDs:**
   - Follow [Azure AD Configuration](#azure-ad-configuration) section
   - Update configuration with correct IDs
   - Restart application

---

#### Issue 7: Application won't start

**Symptoms:**
```
Error creating bean with name 'sharePointUploadService':
Invocation of init method failed
```

**Causes:**
- Invalid configuration values
- Network connectivity issues
- Azure AD service outage

**Solutions:**

**1. Check Configuration Format:**
```properties
# Correct format:
sharepoint.tenant.id=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
sharepoint.client.id=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
sharepoint.client.secret=abC123~xYz789...

# Wrong format (no spaces, no quotes):
sharepoint.tenant.id = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"  # ❌
```

**2. Test Network Connectivity:**
```bash
# Test Azure AD
curl https://login.microsoftonline.com/{tenant-id}/v2.0/.well-known/openid-configuration

# Test MS Graph API
curl https://graph.microsoft.com/v1.0/
```

**3. Enable Debug Logging:**
```properties
logging.level.eduhk.fhr.web.service.SharePointUploadService=DEBUG
logging.level.com.azure.identity=DEBUG
logging.level.com.microsoft.graph=DEBUG
```

**4. Review Stack Trace:**
```bash
# Check full error
grep -A 50 "Error creating bean" catalina.base_IS_UNDEFINED/logs/server.log_RDPS.log
```

---

### Diagnostic Commands

#### Check Application Status
```bash
# Application running?
ps aux | grep java

# Port 8080 listening?
lsof -i :8080

# Recent logs
tail -50 catalina.base_IS_UNDEFINED/logs/server.log_RDPS.log
```

#### Test Azure AD Authentication
```bash
# Get access token manually
curl -X POST \
  "https://login.microsoftonline.com/{tenant-id}/oauth2/v2.0/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id={client-id}" \
  -d "client_secret={client-secret}" \
  -d "scope=https://graph.microsoft.com/.default" \
  -d "grant_type=client_credentials"
```

Expected response:
```json
{
  "token_type": "Bearer",
  "expires_in": 3599,
  "access_token": "eyJ0eXAiOiJKV1QiLCJhbGc..."
}
```

#### Test MS Graph API Access
```bash
# List drives (replace {token}, {site-id})
curl -X GET \
  "https://graph.microsoft.com/v1.0/sites/{site-id}/drives" \
  -H "Authorization: Bearer {token}"
```

Expected response:
```json
{
  "value": [
    {
      "id": "b!aBcD...",
      "name": "Documents",
      "driveType": "documentLibrary"
    }
  ]
}
```

---

## API Reference

### SharePointUploadController Endpoints

#### GET /sharepoint/upload

**Description:** Display file upload page

**Authorization:** `ROLE_USE_RDPS` required

**Request:**
```http
GET /RDPS/sharepoint/upload HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=...
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: text/html

[HTML content with Dropzone.js]
```

---

#### POST /sharepoint/ajax/upload

**Description:** Upload file to SharePoint

**Authorization:** `ROLE_USE_RDPS` required

**Content-Type:** `multipart/form-data`

**Request:**
```http
POST /RDPS/sharepoint/ajax/upload HTTP/1.1
Host: localhost:8080
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary...
Cookie: JSESSIONID=...

------WebKitFormBoundary...
Content-Disposition: form-data; name="file"; filename="document.pdf"
Content-Type: application/pdf

[Binary file data]
------WebKitFormBoundary...
Content-Disposition: form-data; name="_csrf"

csrf-token-value
------WebKitFormBoundary...--
```

**Response (Success):**
```json
{
  "success": true,
  "result": {
    "fileId": "01ABCDEF1234567890ABCDEF1234567890",
    "fileName": "document.pdf",
    "webUrl": "https://contoso.sharepoint.com/sites/HRDocuments/Shared%20Documents/document.pdf",
    "size": 1048576
  },
  "message": null
}
```

**Response (Error - File Too Large):**
```json
{
  "success": false,
  "result": null,
  "message": "Upload failed: File size exceeds maximum: 50 MB"
}
```

**Response (Error - Invalid Type):**
```json
{
  "success": false,
  "result": null,
  "message": "Upload failed: File type not allowed. Allowed: pdf,doc,docx,jpg,jpeg,png,xls,xlsx,txt"
}
```

**Response (Error - Service Not Initialized):**
```json
{
  "success": false,
  "result": null,
  "message": "SharePoint service not initialized. Check configuration."
}
```

---

### Microsoft Graph API Calls

#### Upload File (Small)

**Endpoint:** `PUT /sites/{site-id}/drives/{drive-id}/root:/{filename}:/content`

**Request:**
```http
PUT https://graph.microsoft.com/v1.0/sites/contoso.sharepoint.com,guid1,guid2/drives/b!aBcD.../root:/document.pdf:/content HTTP/1.1
Authorization: Bearer eyJ0eXAiOiJKV1Q...
Content-Type: application/pdf
Content-Length: 1048576

[Binary file data]
```

**Response:**
```json
{
  "@odata.context": "https://graph.microsoft.com/v1.0/$metadata#drives('b%21aBcD...')/root/$entity",
  "id": "01ABCDEF1234567890ABCDEF1234567890",
  "name": "document.pdf",
  "size": 1048576,
  "createdDateTime": "2025-11-25T16:30:00Z",
  "lastModifiedDateTime": "2025-11-25T16:30:00Z",
  "webUrl": "https://contoso.sharepoint.com/sites/HRDocuments/Shared%20Documents/document.pdf",
  "file": {
    "mimeType": "application/pdf",
    "hashes": {
      "quickXorHash": "abc123...",
      "sha1Hash": "def456..."
    }
  }
}
```

---

## Future Enhancements

### Planned Features

#### 1. Database Tracking (Optional)

**Description:** Store upload metadata in RDPS database

**Benefits:**
- Audit trail of who uploaded what
- Link files to candidate records
- Search uploaded files

**Implementation:**
```sql
CREATE TABLE RDPS_SHAREPOINT_UPLOAD (
    UPLOAD_ID NUMBER PRIMARY KEY,
    FILE_NAME VARCHAR2(500) NOT NULL,
    SHAREPOINT_FILE_ID VARCHAR2(100) NOT NULL,
    WEB_URL VARCHAR2(1000) NOT NULL,
    FILE_SIZE NUMBER NOT NULL,
    UPLOADED_BY VARCHAR2(100) NOT NULL,
    UPLOADED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CANDIDATE_ID NUMBER,
    CONSTRAINT FK_CANDIDATE FOREIGN KEY (CANDIDATE_ID)
        REFERENCES RDPS_CANDIDATE(CANDIDATE_ID)
);
```

---

#### 2. Large File Upload Support

**Description:** Support files > 250 MB using upload sessions

**Current Limitation:** 50 MB max (simple PUT)

**Implementation Approach:**
```java
// Create upload session
UploadSession session = graphClient
    .sites(siteId)
    .drives(driveId)
    .root()
    .itemWithPath(filename)
    .createUploadSession(new DriveItemUploadableProperties())
    .buildRequest()
    .post();

// Upload in chunks
LargeFileUploadTask<DriveItem> task = new LargeFileUploadTask<>(
    session,
    graphClient,
    inputStream,
    fileSize,
    DriveItem.class
);

DriveItem item = task.upload(320 * 1024); // 320 KB chunks
```

---

#### 3. Folder Organization

**Description:** Upload files to specific folders based on category

**Example Structure:**
```
SharePoint Site
  └── Document Library
       ├── Resumes/
       ├── Certificates/
       ├── References/
       └── Other/
```

**Implementation:**
```java
// Upload to specific folder
DriveItem uploadedItem = graphClient
    .sites(config.getSiteId())
    .drives(config.getDriveId())
    .root()
    .itemWithPath("Resumes/" + sanitizedFilename) // Folder path
    .content()
    .buildRequest()
    .put(fileBytes);
```

**UI Enhancement:**
```html
<select name="folder" id="folderSelect">
    <option value="Resumes">Resumes</option>
    <option value="Certificates">Certificates</option>
    <option value="References">References</option>
    <option value="Other">Other Documents</option>
</select>
```

---

#### 4. Metadata Tagging

**Description:** Add custom metadata to uploaded files

**Example Metadata:**
- Candidate ID
- Document type
- Upload date
- Department

**Implementation:**
```java
// Create file with metadata
DriveItem driveItem = new DriveItem();
driveItem.name = sanitizedFilename;
driveItem.file = new File();

// Set custom metadata
Map<String, Object> metadata = new HashMap<>();
metadata.put("candidateId", candidateId);
metadata.put("documentType", "Resume");
metadata.put("department", "HR");

driveItem.additionalDataManager().put("fields", new JsonPrimitive(
    new Gson().toJson(metadata)
));

// Upload with metadata
DriveItem uploadedItem = graphClient
    .sites(config.getSiteId())
    .drives(config.getDriveId())
    .root()
    .itemWithPath(sanitizedFilename)
    .content()
    .buildRequest()
    .put(fileBytes);
```

---

#### 5. Upload History View

**Description:** Display list of previously uploaded files

**UI Mockup:**
```
┌─────────────────────────────────────────────────────────┐
│  Upload History                                          │
├────────────┬────────────────┬───────────┬───────────────┤
│ File Name  │ Uploaded By    │ Date      │ SharePoint    │
├────────────┼────────────────┼───────────┼───────────────┤
│ resume.pdf │ fhr-dev-uacct  │ 2025-11-2 │ [View]        │
│ cert.jpg   │ fhr-dev-uacct  │ 2025-11-2 │ [View]        │
│ ref.docx   │ john.doe       │ 2025-11-2 │ [View]        │
└────────────┴────────────────┴───────────┴───────────────┘
```

**Endpoint:**
```java
@GetMapping("/history")
public String viewHistory(Model model) {
    List<UploadRecord> uploads = uploadHistoryService.getRecentUploads();
    model.addAttribute("uploads", uploads);
    return "sharepoint/history";
}
```

---

#### 6. Duplicate Detection

**Description:** Warn user if file with same name exists

**Implementation:**
```java
// Check if file exists
try {
    DriveItem existing = graphClient
        .sites(config.getSiteId())
        .drives(config.getDriveId())
        .root()
        .itemWithPath(sanitizedFilename)
        .buildRequest()
        .get();

    // File exists - prompt user
    return AjaxResponse.error(
        "File already exists. Overwrite? (Yes/No)"
    );

} catch (GraphServiceException e) {
    if (e.getResponseCode() == 404) {
        // File doesn't exist - proceed with upload
    }
}
```

---

#### 7. Virus Scanning Integration

**Description:** Scan files for viruses before upload

**Options:**

**Option 1: ClamAV (Open Source)**
```java
ClamAvClient clamAv = new ClamAvClient("localhost", 3310);
ScanResult result = clamAv.scan(file.getInputStream());

if (result.getStatus() == ClamAvClient.Status.PASSED) {
    // Safe - proceed with upload
} else {
    throw new Exception("Virus detected: " + result.getSignature());
}
```

**Option 2: Microsoft Defender for Cloud Apps**
- Automatic scanning in SharePoint
- Policy-based blocking
- No code changes required

---

#### 8. Bulk Upload Support

**Description:** Upload multiple files in one operation

**UI Enhancement:**
```javascript
// Dropzone supports multiple files by default
maxFiles: 10, // Allow up to 10 files at once
parallelUploads: 3, // Upload 3 files in parallel
```

**Backend Optimization:**
```java
@PostMapping("/ajax/upload-bulk")
@ResponseBody
public AjaxResponse uploadMultiple(
    @RequestParam("files") MultipartFile[] files
) {
    List<Map<String, Object>> results = new ArrayList<>();

    for (MultipartFile file : files) {
        try {
            DriveItem item = uploadService.uploadFile(file);
            results.add(buildResult(item));
        } catch (Exception e) {
            results.add(buildError(file.getOriginalFilename(), e));
        }
    }

    return AjaxResponse.success(results);
}
```

---

#### 9. Progress Tracking for Large Files

**Description:** Real-time progress updates for large uploads

**Implementation:**
```java
// Server-sent events for progress
@GetMapping("/upload-progress/{uploadId}")
public SseEmitter trackProgress(@PathVariable String uploadId) {
    SseEmitter emitter = new SseEmitter();

    uploadProgressService.registerListener(uploadId, progress -> {
        try {
            emitter.send(SseEmitter.event()
                .name("progress")
                .data(progress));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    });

    return emitter;
}
```

**Frontend:**
```javascript
const eventSource = new EventSource('/sharepoint/upload-progress/' + uploadId);
eventSource.addEventListener('progress', function(e) {
    const progress = JSON.parse(e.data);
    updateProgressBar(progress.percentage);
});
```

---

#### 10. Email Notifications

**Description:** Send email when upload completes

**Integration with Existing Email System:**
```java
// RDPS already has email job system
EmailJob emailJob = new EmailJob();
emailJob.setToAddress(user.getEmail());
emailJob.setSubject("File Uploaded to SharePoint");
emailJob.setBody("Your file '" + filename + "' has been uploaded.\n\n" +
                 "SharePoint URL: " + webUrl);
emailJob.setStatus("PENDING");

emailJobDao.insert(emailJob);
```

---

### Technical Debt Items

#### 1. Error Handling Improvements

**Current State:** Generic exceptions

**Improvement:**
```java
// Custom exception hierarchy
public class SharePointException extends Exception {
    private final ErrorCode errorCode;
    private final String userMessage;

    public enum ErrorCode {
        AUTHENTICATION_FAILED,
        PERMISSION_DENIED,
        FILE_TOO_LARGE,
        INVALID_FILE_TYPE,
        NETWORK_ERROR,
        SHAREPOINT_UNAVAILABLE
    }
}
```

---

#### 2. Retry Logic

**Current State:** No retries on transient failures

**Improvement:**
```java
@Retryable(
    value = {GraphServiceException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
public DriveItem uploadFile(MultipartFile file) throws Exception {
    // Upload logic
}
```

---

#### 3. Circuit Breaker

**Current State:** No protection against cascading failures

**Improvement:**
```java
@CircuitBreaker(name = "sharepoint", fallbackMethod = "uploadFallback")
public DriveItem uploadFile(MultipartFile file) throws Exception {
    // Upload logic
}

public DriveItem uploadFallback(MultipartFile file, Exception e) {
    logger.error("Circuit breaker activated for SharePoint upload", e);
    throw new ServiceUnavailableException("SharePoint service temporarily unavailable");
}
```

---

#### 4. Caching

**Current State:** New token requested for each upload

**Improvement:**
```java
// Cache access tokens (they're valid for 1 hour)
@Cacheable(value = "accessTokens", key = "#tenantId")
public String getAccessToken(String tenantId) {
    // Get token from Azure AD
}
```

---

#### 5. Metrics and Monitoring

**Current State:** Basic logging only

**Improvement:**
```java
@Timed(value = "sharepoint.upload", description = "SharePoint upload duration")
@Counted(value = "sharepoint.upload.count", description = "SharePoint upload count")
public DriveItem uploadFile(MultipartFile file) throws Exception {
    // Upload logic

    meterRegistry.counter("sharepoint.upload.size",
        "file_type", getFileExtension(file))
        .increment(file.getSize());
}
```

---

## Appendix

### A. Configuration Template

```properties
# ==============================================================================
# SharePoint Upload Configuration
# ==============================================================================
# This file configures the SharePoint integration for RDPS application.
#
# IMPORTANT:
# - Do not commit this file with real credentials to version control
# - Use environment variables or Azure Key Vault in production
# - Rotate client secrets every 12-24 months
# ==============================================================================

# ------------------------------------------------------------------------------
# Azure AD Application Registration
# ------------------------------------------------------------------------------
# How to obtain:
# 1. Azure Portal → Azure Active Directory → App registrations
# 2. Find your application (e.g., "RDPS SharePoint Integration")
# 3. Overview page shows Application (client) ID and Directory (tenant) ID
# ------------------------------------------------------------------------------

# Azure AD Tenant (Directory) ID
# Format: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
sharepoint.tenant.id=

# Application (Client) ID
# Format: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
sharepoint.client.id=

# Client Secret Value (NOT the Secret ID)
# Format: Random alphanumeric string with special characters
# Location: App registrations → Certificates & secrets → Client secrets
# NOTE: Shown only once when created - store securely!
sharepoint.client.secret=

# ------------------------------------------------------------------------------
# SharePoint Site and Drive Configuration
# ------------------------------------------------------------------------------
# How to obtain Site ID:
# Method 1: Graph Explorer
#   GET https://graph.microsoft.com/v1.0/sites?search=YOUR_SITE_NAME
#
# Method 2: Direct URL
#   GET https://graph.microsoft.com/v1.0/sites/contoso.sharepoint.com:/sites/HRDocuments
#
# How to obtain Drive ID:
#   GET https://graph.microsoft.com/v1.0/sites/{site-id}/drives
#   Find the drive with name "Documents" (or your library name)
# ------------------------------------------------------------------------------

# SharePoint Site ID (full format with hostname and GUIDs)
# Format: contoso.sharepoint.com,GUID1,GUID2
# Example: contoso.sharepoint.com,a1b2c3d4-...,e5f6g7h8-...
sharepoint.site.id=

# SharePoint Drive ID (Document Library ID)
# Format: b!BASE64_ENCODED_STRING
# Example: b!aBcD1234eFgH5678...
sharepoint.drive.id=

# ------------------------------------------------------------------------------
# Upload Validation Settings
# ------------------------------------------------------------------------------

# Maximum file size in megabytes (MB)
# Recommendation: 50 MB for most use cases
# Note: Files > 250 MB require upload session (not currently supported)
sharepoint.max.file.size.mb=50

# Allowed file extensions (comma-separated, no spaces, no dots)
# Common document types included by default
# Modify based on your security policy and business needs
#
# Document formats:  pdf, doc, docx, txt, rtf, odt
# Spreadsheets:      xls, xlsx, csv, ods
# Images:            jpg, jpeg, png, gif, bmp, tiff
# Archives:          zip, rar (use with caution)
#
# Security Note: Never allow executable files (.exe, .bat, .sh, .ps1, .cmd)
sharepoint.allowed.extensions=pdf,doc,docx,jpg,jpeg,png,xls,xlsx,txt

# ==============================================================================
# End of Configuration
# ==============================================================================
```

---

### B. Build and Deployment

#### Build Commands
```bash
# Clean build (skip tests)
mvn clean package -DskipTests

# Build with tests
mvn clean package

# Build specific profile
mvn clean package -P production

# Build and install to local repo
mvn clean install
```

#### Run Commands
```bash
# Development mode (with hot reload)
mvn spring-boot:run

# Production mode (as WAR)
java -jar target/RDPS-0.0.1-SNAPSHOT.war

# With specific profile
mvn spring-boot:run -Dspring.profiles.active=production

# With JVM options
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx2048m -Xms1024m"
```

#### Deployment Checklist

**Pre-Deployment:**
- [ ] Azure AD app registration completed
- [ ] API permissions granted and consented
- [ ] Client secret created and stored securely
- [ ] SharePoint site and drive IDs obtained
- [ ] Configuration file updated with production values
- [ ] Configuration file excluded from version control
- [ ] Application built successfully (`mvn clean package`)
- [ ] Unit tests passed (if enabled)

**Deployment:**
- [ ] WAR file deployed to application server
- [ ] Application started successfully
- [ ] Initialization logs reviewed (SharePoint service initialized)
- [ ] Health check endpoint verified
- [ ] Upload page accessible
- [ ] Test upload completed successfully

**Post-Deployment:**
- [ ] Monitoring configured (application logs, Azure AD logs)
- [ ] Alerts configured (failed uploads, authentication errors)
- [ ] Documentation updated with production URLs
- [ ] User training completed
- [ ] Support team notified

---

### C. Testing Guide

#### Manual Testing Scenarios

**Test 1: Valid File Upload**
1. Navigate to `/sharepoint/upload`
2. Upload valid PDF file (< 50 MB)
3. **Expected:** Success notification, SharePoint URL displayed
4. Click SharePoint URL
5. **Expected:** File opens in SharePoint

**Test 2: File Too Large**
1. Navigate to `/sharepoint/upload`
2. Upload file > 50 MB
3. **Expected:** Error message "File size exceeds maximum: 50 MB"

**Test 3: Invalid File Type**
1. Navigate to `/sharepoint/upload`
2. Upload .exe file
3. **Expected:** Error message "File type not allowed..."

**Test 4: Special Characters in Filename**
1. Navigate to `/sharepoint/upload`
2. Upload file with name: `test:file?.pdf`
3. **Expected:** Success, filename sanitized to `test_file_.pdf`

**Test 5: Duplicate Filename**
1. Upload file `test.pdf`
2. Upload same file again
3. **Expected:** SharePoint overwrites (no error)

**Test 6: Multiple Files**
1. Navigate to `/sharepoint/upload`
2. Drag 5 files at once
3. **Expected:** All files upload successfully

**Test 7: Network Interruption**
1. Start large file upload
2. Disable network during upload
3. **Expected:** Error message, file not uploaded

**Test 8: Unauthorized Access**
1. Login as user without `ROLE_USE_RDPS`
2. Navigate to `/sharepoint/upload`
3. **Expected:** Access Denied (403)

---

### D. Glossary

| Term | Definition |
|------|------------|
| **Azure AD** | Azure Active Directory - Microsoft's cloud-based identity service |
| **Client Credentials** | OAuth 2.0 flow for app-only authentication (no user interaction) |
| **Client ID** | Unique identifier for Azure AD application registration |
| **Client Secret** | Password for Azure AD application (like API key) |
| **Drive** | SharePoint document library (storage location) |
| **Drive ID** | Unique identifier for SharePoint document library |
| **DriveItem** | Microsoft Graph API object representing a file or folder |
| **Dropzone.js** | JavaScript library for drag-and-drop file uploads |
| **Graph API** | Microsoft's unified API for accessing Microsoft 365 data |
| **MS Graph SDK** | Java library for calling Microsoft Graph API |
| **Site ID** | Unique identifier for SharePoint site |
| **Tenant ID** | Unique identifier for Azure AD organization |
| **Token** | JWT access token for authenticating API requests |

---

### E. References

#### Official Documentation
- [Microsoft Graph API Reference](https://docs.microsoft.com/en-us/graph/api/overview)
- [Microsoft Graph SDK for Java](https://github.com/microsoftgraph/msgraph-sdk-java)
- [Azure Identity SDK](https://docs.microsoft.com/en-us/java/api/overview/azure/identity-readme)
- [SharePoint REST API](https://docs.microsoft.com/en-us/sharepoint/dev/sp-add-ins/working-with-files-and-folders-by-using-rest)
- [Dropzone.js Documentation](https://www.dropzone.dev/js/)

#### Related RDPS Documentation
- `/CLAUDE.md` - Project overview and build instructions
- `/db_scripts/README.md` - Database schema documentation
- TalentLink integration patterns (reference for similar implementations)

#### Azure AD Setup Guides
- [Register an application](https://docs.microsoft.com/en-us/azure/active-directory/develop/quickstart-register-app)
- [Configure app permissions](https://docs.microsoft.com/en-us/azure/active-directory/develop/quickstart-configure-app-access-web-apis)
- [Client credentials flow](https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-client-creds-grant-flow)

---

## Document Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-11-25 | Development Team | Initial documentation |

---

**END OF DOCUMENT**
