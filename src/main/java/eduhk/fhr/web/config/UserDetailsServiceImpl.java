package eduhk.fhr.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import eduhk.fhr.web.dao.UserDao;
import eduhk.fhr.web.model.User;
import eduhk.fhr.web.model.UserDetailsImp;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	@Autowired
	private UserDao userDao;

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userDao.findByUsername(username);
		UserDetailsImp userDetails = new UserDetailsImp();
		userDetails.setUser(user);
		return userDetails;
	}
}
