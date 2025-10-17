package eduhk.fhr.web.util;

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.servlet.http.Part;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.ObjectError;

import eduhk.fhr.web.model.User;
import eduhk.fhr.web.model.UserDetailsImp;

public class Common {
	
	public static String nvl(String s, String replacement) {
		return (s != null) ? (!s.equals("")) ? s : replacement : replacement;
	}

	public static String null2Empty(String s) {
		return nvl(s, "").trim();
	}
	
	public static String[] null2Empty(String[] s) {
		return (s == null || s.length == 0) ? new String[] {""} : s; 
	}
	
	public static String getUserstamp(Authentication auth) {
		return null2Empty(((UserDetailsImp)auth.getPrincipal()).getUsername());
	}
	
	public static String getUTF8(String str) {
		return new String(Common.null2Empty(str).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
	}
	
	public static String getValue(ArrayList<String> list, int index) {
		try {
			return null2Empty(list.get(index));
		} catch (Exception e) {
			return "";
		}
	}

	public static Date stringToDate(String dateString) {
		try {
			String paramDateFormat = "dd/MM/yyyy HH:mm:ss";
			Date date = new SimpleDateFormat(paramDateFormat).parse(dateString);
			return date;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Date getTodayDate() {
		try {
			Date date = new Date();
			return date;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**** Handle 'IN' SQL in PreparedStatement ****/	
	public static String preparePlaceHolders(int length) {
	    return String.join(",", Collections.nCopies(length, "?"));
	}
	
	public static int setValues(PreparedStatement preparedStatement, int startingIndex, Object[] values) throws SQLException {
		int i = startingIndex;
		int arrayIndex = 0;
	    for (i = startingIndex; i <= values.length + (startingIndex-1); i++) {
	        preparedStatement.setObject(i, values[arrayIndex++]);
	    }
	    return i;
	}
	
	public static String printErrors(TreeSet<String> allErrors) {
		StringBuilder errorSb = new StringBuilder();
		if (!allErrors.isEmpty()) {			
			errorSb.append("There are "+ allErrors.size() +" error(s):").append("<br/>");
			errorSb.append("<ol>");
			for (String error : allErrors) {
				errorSb.append("<li>" + error + "</li>");
			}
			errorSb.append("</ol>");
		}
		return errorSb.toString();
	}
	
	public static String printErrors(List<ObjectError> allErrors) {
		TreeSet<String> errors = new TreeSet<>(allErrors.stream().map(v->v.getDefaultMessage()).collect(Collectors.toSet()));
		return printErrors(errors);
	}	
	
	public static String printErrors(List<ObjectError> allErrors, MessageSource messageSource, Locale locale) {
		TreeSet<String> errors = new TreeSet<>();
		for (ObjectError objectError : allErrors) {
			String message = "";
			try {
				message = messageSource.getMessage(objectError.getCode(), objectError.getArguments(), locale);
			} catch (Exception e) {
				message = objectError.getDefaultMessage();
			}
			errors.add(message);
		}
		return printErrors(errors);
	}
	
	public static String getExcelCellValue(Cell cell) {
		String result = "";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy");
		if(cell != null) {
	        switch (cell.getCellTypeEnum()) {
	        case STRING:
	            result = cell.getRichStringCellValue().getString();
	            break;
	        case NUMERIC:
	            if (DateUtil.isCellDateFormatted(cell)) {
					Date dateVal = cell.getDateCellValue();
					result = sdf.format(dateVal);
	            } else {
	                double numVal = cell.getNumericCellValue();
	                if (Math.floor(numVal) == numVal) { //int
	                	result = String.valueOf((long) numVal);
	                } else {
	                	result = String.valueOf(numVal);	
	                }
	            }
	            break;
	        case BOOLEAN:
	            cell.getBooleanCellValue();
	            break;
	        case FORMULA:
//	            result = cell.getCellFormula().toString();
	        	CellType type = cell.getCachedFormulaResultTypeEnum();
	        	switch (type) {
	        	case STRING: 
		            result = cell.getRichStringCellValue().getString();
		            break;
		        case NUMERIC:
		            if (DateUtil.isCellDateFormatted(cell)) {
						Date dateVal = cell.getDateCellValue();
						result = sdf.format(dateVal);
		            } else {
		                double numVal = cell.getNumericCellValue();
		                if (Math.floor(numVal) == numVal) { //int
		                	result = String.valueOf((long) numVal);
		                } else {
		                	result = String.valueOf(numVal);	
		                }
		            }
		            break;
				default:
					break; 
	        	}
	            break;
	        case BLANK:
	            break;
	        default:
	        }		
		}
		return result.trim();
	}	
	
	public static void printException(Exception e, String userstamp, boolean printStackTrace) {
		try {
			StackTraceElement[] ste = new Throwable().getStackTrace();
			userstamp = Common.null2Empty(userstamp);
			Logger logger = LoggerFactory.getLogger(e.getClass());
			logger.error(e.getClass().getName() + " in " + ste[1] + ": "+ e.getMessage() +"\n\t\tat " + ste[2] + "\n\t\t" + "Userstamp: " + userstamp);
		} catch (Exception re) {
			printException(re, userstamp, true);
		}

	}
	
	public static String getFileName(Part part) {
	    String contentDisp = part.getHeader("content-disposition");
	    Pattern pattern = Pattern.compile("filename=\"(.*?)\"");
	    Matcher matcher = pattern.matcher(contentDisp);
	    if (matcher.find()) {
	    	return Common.null2Empty(matcher.group(1)).replaceAll("'", "_");
	    } else {
	    	return "";
	    }
	}
	
	public static boolean hasRole(String targetRole) {
		ArrayList<GrantedAuthority> authList = new ArrayList<>(SecurityContextHolder.getContext().getAuthentication().getAuthorities());
		boolean result = false;
		for (GrantedAuthority ga: authList) {
			if (ga.getAuthority().equals(targetRole)) {
				result = true;
				break;
			}
		}
		return result;
	}

	public static boolean isValidDate(String dateStr) {
		if (Common.null2Empty(dateStr).equals("")) {
			return true;
		}
		try {
			LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static User getCurrentLoggedInUser() {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsImp userDetails = (UserDetailsImp) authentication.getPrincipal();
			User user = userDetails.getUser();
			return user;
		} catch (Exception e) {
			Common.printException(e, null, false);			
			return null;
		}
		
	}	

}
