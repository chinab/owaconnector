/*
 * DavMail POP/IMAP/SMTP/CalDav/LDAP Exchange Gateway
 * Copyright (C) 2009  Mickael Guessant
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package davmail.exchange;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import davmail.BundleMessage;
import davmail.exception.DavMailException;

/**
 * Create ExchangeSession instances.
 */
public final class ExchangeSessionFactory {
    private static final Object LOCK = new Object();
    private static final Map<PoolKey, ExchangeSession> POOL_MAP = new HashMap<PoolKey, ExchangeSession>();
    private static boolean configChecked;
    private static boolean errorSent;

    static class PoolKey {
        final String url;
        final String userName;
        final String password;

        PoolKey(String url, String userName, String password) {
            this.url = url;
            this.userName = userName;
            this.password = password;
        }

        @Override
        public boolean equals(Object object) {
            return object == this ||
                    object instanceof PoolKey &&
                            ((PoolKey) object).url.equals(this.url) &&
                            ((PoolKey) object).userName.equals(this.userName) &&
                            ((PoolKey) object).password.equals(this.password);
        }

        @Override
        public int hashCode() {
            return url.hashCode() + userName.hashCode() + password.hashCode();
        }
    }

    private ExchangeSessionFactory() {
    }

    /**
     * Create authenticated Exchange session
     *
     * @param userName user login
     * @param password user password
     * @return authenticated session
     * @throws IOException on error
     * @throws DavMailException 
     */
    public static ExchangeSession getInstance(URI url, String userName, String password) throws IOException, DavMailException {
        ExchangeSession session = null;
        try {
            PoolKey poolKey = new PoolKey(url.toURL().toExternalForm(), userName, password);

            synchronized (LOCK) {
                session = POOL_MAP.get(poolKey);
            }
            if (session != null) {
                ExchangeSession.LOGGER.debug("Got session " + session + " from cache");
            }

            if (session != null && session.isExpired()) {
                ExchangeSession.LOGGER.debug("Session " + session + " expired");
                session = null;
                // expired session, remove from cache
                synchronized (LOCK) {
                    POOL_MAP.remove(poolKey);
                }
            }

            if (session == null) {
                session = new ExchangeSession(poolKey.url, poolKey.userName, poolKey.password);
                ExchangeSession.LOGGER.debug("Created new session: " + session);
            }
            // successfull login, put session in cache
            synchronized (LOCK) {
                POOL_MAP.put(poolKey, session);
            }
            // session opened, future failure will mean network down
            configChecked = true;
            // Reset so next time an problem occurs message will be sent once
            errorSent = false;
        
        } catch (Exception exc) {
        	exc.printStackTrace();
            handleException(exc);
        }
        return session;
    }

    /**
     * Get a non expired session.
     * If the current session is not expired, return current session, else try to create a new session
     *
     * @param currentSession current session
     * @param userName       user login
     * @param password       user password
     * @return authenticated session
     * @throws IOException on error
     * @throws DavMailException 
     */
    public static ExchangeSession getInstance(ExchangeSession currentSession, URI url, String userName, String password)
            throws IOException, DavMailException {
        ExchangeSession session = currentSession;
        try {
            if (session.isExpired()) {
                session = null;
                
                PoolKey poolKey = new PoolKey(url.toURL().toExternalForm(), userName, password);
                // expired session, remove from cache
                synchronized (LOCK) {
                    POOL_MAP.remove(poolKey);
                }
                session = getInstance(url,userName, password);
            }
       
        } catch (Exception exc) {
            handleException(exc);
        }
        return session;
    }

  
    private static void handleException(Exception exc) throws DavMailException {
//        if (configChecked) {
//            ExchangeSession.LOGGER.warn(BundleMessage.formatLog("EXCEPTION_NETWORK_DOWN"));
//            throw new NetworkDownException("EXCEPTION_NETWORK_DOWN");
//        } else {
            BundleMessage message = new BundleMessage("EXCEPTION_CONNECT", exc.getClass().getName(), exc.getMessage());
            if (errorSent) {
                ExchangeSession.LOGGER.warn(message);
                throw new NetworkDownException("EXCEPTION_DAVMAIL_CONFIGURATION", message);
            } else {
                // Mark that an error has been sent so you only get one
                // error in a row (not a repeating string of errors).
                errorSent = true;
                ExchangeSession.LOGGER.error(message);
                throw new DavMailException("EXCEPTION_DAVMAIL_CONFIGURATION", message);
            }
//        }
    }


    /**
     * Reset config check status and clear session pool.
     */
    public static void reset() {
        configChecked = false;
        errorSent = false;
        POOL_MAP.clear();
    }
}
