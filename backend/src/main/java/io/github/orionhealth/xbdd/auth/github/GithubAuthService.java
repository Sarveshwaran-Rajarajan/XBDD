package io.github.orionhealth.xbdd.auth.github;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.orionhealth.xbdd.model.auth.GithubProfile;
import io.github.orionhealth.xbdd.model.auth.GithubToken;
import io.github.orionhealth.xbdd.model.common.LoginType;
import io.github.orionhealth.xbdd.model.common.User;
import io.github.orionhealth.xbdd.persistence.UsersDao;

@Service
public class GithubAuthService {

	@Autowired
	UsersDao usersDao;

	@Autowired
	GithubClient githubAuthClient;

	public User authenticateUser(final String githubAuthCode) throws IOException {
		final GithubToken token = this.githubAuthClient.getAuthToken(githubAuthCode);
		final GithubProfile githubProfile = this.githubAuthClient.getProfileData(token);

		final User savedUser = this.usersDao.saveUser(mapGithubProfileToUser(githubProfile));

		return savedUser;
	}

	private User mapGithubProfileToUser(final GithubProfile githubProfile) {
		final User user = new User();

		user.setLoginType(LoginType.GITHUB);
		user.setDisplay(StringUtils.defaultIfBlank(githubProfile.getName(), githubProfile.getLogin()));
		user.setSocialId(githubProfile.getId());
		user.setSocialLogin(githubProfile.getLogin());
		user.setUserId(String.format("github-%s", githubProfile.getId()));

		return user;
	}
}
