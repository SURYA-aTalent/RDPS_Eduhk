package eduhk.fhr.web.service;

import jakarta.annotation.PostConstruct;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eduhk.fhr.web.model.TalentLinkUserStaging;
import eduhk.fhr.web.soap.handler.TalentLinkSOAPHandler;
import eduhk.fhr.web.soap.user.DayOfWeek;
import eduhk.fhr.web.soap.user.LangCode;
import eduhk.fhr.web.soap.user.RequestByGetUsersDto;
import eduhk.fhr.web.soap.user.UserDto;
import eduhk.fhr.web.soap.user.UserExtDto;
import eduhk.fhr.web.soap.user.UserStatus;
import eduhk.fhr.web.soap.user.UserWebService;
import eduhk.fhr.web.soap.user.UserWebService_Service;

/**
 * SOAP-based service for TalentLink User operations
 * Uses JAX-WS generated stubs with WS-Security authentication
 */
@Service
public class TalentLinkSOAPUserService {

    private static final Logger logger = LoggerFactory.getLogger(TalentLinkSOAPUserService.class);

    @Autowired
    private ParameterService parameterService;

    private UserWebService userService;
    private boolean initialized = false;

    /**
     * Initialize SOAP service with credentials and endpoint
     */
    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing TalentLink SOAP User Service");

            // Get credentials from parameter service
            String username = parameterService.getParameterValue("TALENTLINK_USERNAME");
            String password = parameterService.getParameterValue("TALENTLINK_PASSWORD");
            String apiKey = parameterService.getParameterValue("TALENTLINK_API_KEY");

            // Set credentials in SOAP handler
            TalentLinkSOAPHandler.setCredentials(username, password);

            // Create SOAP service
            UserWebService_Service service = new UserWebService_Service();
            userService = service.getUserWebServicePort();

            // Add SOAP handler programmatically
            BindingProvider bindingProvider = (BindingProvider) userService;
            java.util.List<jakarta.xml.ws.handler.Handler> handlerChain = new java.util.ArrayList<>();
            handlerChain.add(new TalentLinkSOAPHandler());
            bindingProvider.getBinding().setHandlerChain(handlerChain);

            // Set endpoint with API key
            String endpoint = getTalentLinkUserSoapUrl() + "?api_key=" + apiKey;
            bindingProvider.getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                endpoint
            );

            initialized = true;
            logger.info("TalentLink SOAP User Service initialized successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize TalentLink SOAP User Service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize SOAP service", e);
        }
    }

    /**
     * Create user in TalentLink via SOAP
     *
     * @param user User to create
     * @return Created user ID
     * @throws Exception if creation fails
     */
    public Long createUser(TalentLinkUserStaging user) throws Exception {
        ensureInitialized();

        logger.info("Creating user via SOAP: {} {}", user.getFirstName(), user.getLastName());

        try {
            Holder<UserExtDto> userHolder = new Holder<>();
            UserExtDto userDto = new UserExtDto();

            // Map staging user to SOAP DTO
            userDto.setFirstName(user.getFirstName());
            userDto.setLastName(user.getLastName());
            userDto.setEmail(user.getEmail());
            userDto.setLogin(user.getLoginUsername());

            // Set language
            userDto.setLanguage(mapLanguage(user.getLanguage()));

            // Set timezone
            userDto.setTimeZone(user.getTimezone() != null ? user.getTimezone() : "Asia/Hong Kong");

            // Set user type
            userDto.setType(mapUserType(user.getUserType()));

            // Set password if provided
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                userDto.setPassword(user.getPassword());
            }

            // Set week start day
            userDto.setFirstDayOfWeek(mapWeekStartDay(user.getWeekStartOn()));

            // Set active status
            userDto.setActive("Active".equalsIgnoreCase(user.getStatus()));

            userHolder.value = userDto;

            // Map activation type
            String activationType = mapActivationType(user.getActivationType());

            // Call SOAP service
            userService.createUser(userHolder, activationType);

            // Get created user ID
            Long userId = userHolder.value.getId();
            logger.info("User created successfully via SOAP with ID: {}", userId);

            return userId;

        } catch (Exception e) {
            logger.error("Failed to create user via SOAP: {}", e.getMessage(), e);
            throw new Exception("Failed to create user in TalentLink: " + e.getMessage(), e);
        }
    }

    /**
     * Update user in TalentLink via SOAP
     *
     * @param userId User ID to update
     * @param user Updated user data
     * @throws Exception if update fails
     */
    public void updateUser(Long userId, TalentLinkUserStaging user) throws Exception {
        ensureInitialized();

        logger.info("Updating user via SOAP: {}", userId);

        try {
            UserExtDto userDto = new UserExtDto();
            userDto.setId(userId);
            userDto.setFirstName(user.getFirstName());
            userDto.setLastName(user.getLastName());
            userDto.setEmail(user.getEmail());
            userDto.setLogin(user.getLoginUsername());
            userDto.setLanguage(mapLanguage(user.getLanguage()));
            userDto.setTimeZone(user.getTimezone());
            userDto.setType(mapUserType(user.getUserType()));
            userDto.setActive("Active".equalsIgnoreCase(user.getStatus()));
            userDto.setFirstDayOfWeek(mapWeekStartDay(user.getWeekStartOn()));

            userService.updateUser(userDto);

            logger.info("User updated successfully via SOAP: {}", userId);

        } catch (Exception e) {
            logger.error("Failed to update user via SOAP: {}", e.getMessage(), e);
            throw new Exception("Failed to update user in TalentLink: " + e.getMessage(), e);
        }
    }

    /**
     * Get user by email or login
     *
     * @param email Email or login to search
     * @return User DTO if found, null otherwise
     */
    public UserDto getUserByEmailOrLogin(String email) {
        ensureInitialized();

        try {
            RequestByGetUsersDto request = new RequestByGetUsersDto();
            request.setUserStatus(UserStatus.ALL);
            request.setEmail(email);

            java.util.List<UserDto> users = userService.getUsers(request);

            if (users != null && !users.isEmpty()) {
                return users.get(0);
            }

            return null;

        } catch (Exception e) {
            logger.error("Failed to get user by email: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Deactivate user in TalentLink
     *
     * @param userId User ID to deactivate
     * @throws Exception if deactivation fails
     */
    public void deactivateUser(Long userId) throws Exception {
        ensureInitialized();

        logger.info("Deactivating user via SOAP: {}", userId);

        try {
            userService.deactivateUser(userId);
            logger.info("User deactivated successfully via SOAP: {}", userId);

        } catch (Exception e) {
            logger.error("Failed to deactivate user via SOAP: {}", e.getMessage(), e);
            throw new Exception("Failed to deactivate user in TalentLink: " + e.getMessage(), e);
        }
    }

    // Helper methods

    private void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("TalentLink SOAP User Service not initialized");
        }
    }

    private String getTalentLinkUserSoapUrl() {
        String url = parameterService.getParameterValue("TALENTLINK_USER_SOAP_URL");
        if (url == null || url.isEmpty()) {
            // Default to TalentLink SOAP endpoint
            url = "https://api3.lumesse-talenthub.com/User/SOAP/User";
            logger.warn("TALENTLINK_USER_SOAP_URL not configured, using default: {}", url);
        }
        return url;
    }

    private LangCode mapLanguage(String language) {
        if (language == null) {
            return LangCode.UK; // Default to English UK
        }

        // Map language string to LangCode enum
        if (language.toLowerCase().contains("english") || language.toLowerCase().contains("uk")) {
            return LangCode.UK;
        } else if (language.toLowerCase().contains("chinese") || language.toLowerCase().contains("cn")) {
            return LangCode.CN;
        } else if (language.toLowerCase().contains("french")) {
            return LangCode.FR;
        } else if (language.toLowerCase().contains("german")) {
            return LangCode.DE;
        } else {
            return LangCode.UK; // Default
        }
    }

    private DayOfWeek mapWeekStartDay(String weekStart) {
        if (weekStart == null || weekStart.isEmpty()) {
            return DayOfWeek.SUNDAY; // Default
        }

        switch (weekStart.toLowerCase()) {
            case "monday":
                return DayOfWeek.MONDAY;
            case "tuesday":
                return DayOfWeek.TUESDAY;
            case "wednesday":
                return DayOfWeek.WEDNESDAY;
            case "thursday":
                return DayOfWeek.THURSDAY;
            case "friday":
                return DayOfWeek.FRIDAY;
            case "saturday":
                return DayOfWeek.SATURDAY;
            case "sunday":
            default:
                return DayOfWeek.SUNDAY;
        }
    }

    private String mapUserType(String userType) {
        if (userType == null || userType.isEmpty()) {
            return "ADVANCED"; // Default
        }

        // Map to TalentLink user types
        if (userType.toLowerCase().contains("advanced")) {
            return "ADVANCED";
        } else if (userType.toLowerCase().contains("basic")) {
            return "BASIC";
        } else if (userType.toLowerCase().contains("mss")) {
            return "MSS";
        } else {
            return "ADVANCED"; // Default
        }
    }

    private String mapActivationType(String activationType) {
        if (activationType == null || activationType.isEmpty()) {
            return "ACTIVATEUSERNOWWITHOUTSENDINGEMAILNOTIFICATION";
        }

        if (activationType.toLowerCase().contains("without") ||
            activationType.toLowerCase().contains("no notification")) {
            return "ACTIVATEUSERNOWWITHOUTSENDINGEMAILNOTIFICATION";
        } else if (activationType.toLowerCase().contains("with") ||
                   activationType.toLowerCase().contains("send")) {
            return "ACTIVATEUSERNOWANDSENDEMAILNOTIFICATION";
        } else {
            return "ACTIVATEUSERNOWWITHOUTSENDINGEMAILNOTIFICATION"; // Default
        }
    }
}
