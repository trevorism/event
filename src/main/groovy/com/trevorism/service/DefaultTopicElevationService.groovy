package com.trevorism.service

import com.google.gson.Gson
import com.trevorism.PropertiesProvider
import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.https.AppClientSecureHttpClient
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.ElevatedTopic
import com.trevorism.model.InternalTokenRequest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentHashMap

@Singleton
class DefaultTopicElevationService implements TopicElevationService {

    private static final Logger log = LoggerFactory.getLogger(DefaultTopicElevationService.class.name)
    private static final String INTERNAL_TOKEN_URL = "https://auth.trevorism.com/token/internal"
    private static final String CLIENT_ID_PROPERTY = "clientId"
    private static final long ALLOWLIST_TTL_MILLIS = 300000
    private static final long TOKEN_REFRESH_MILLIS = 5400000

    private final Gson gson = new Gson()
    private final Map<String, String> tokensByTenant = new ConcurrentHashMap<>()
    private final Map<String, Long> tokenMintedAt = new ConcurrentHashMap<>()

    private PropertiesProvider propertiesProvider
    private SecureHttpClient secureHttpClient
    private Repository<ElevatedTopic> repository

    private volatile Set<String> cachedTopics = null
    private volatile long topicsCachedAt = 0

    @Inject
    DefaultTopicElevationService(PropertiesProvider propertiesProvider) {
        this(propertiesProvider, new AppClientSecureHttpClient())
    }

    DefaultTopicElevationService(PropertiesProvider propertiesProvider, SecureHttpClient secureHttpClient) {
        this.propertiesProvider = propertiesProvider
        this.secureHttpClient = secureHttpClient
        this.repository = new FastDatastoreRepository<>(ElevatedTopic, secureHttpClient)
    }

    @Override
    boolean requiresElevation(String topic) {
        if (!topic) {
            return false
        }
        return loadElevatedTopicNames().contains(topic)
    }

    @Override
    String mintInternalToken(String tenantId) {
        String key = tenantId ?: ""
        String cached = tokensByTenant.get(key)
        Long mintedAt = tokenMintedAt.get(key)
        if (cached && mintedAt != null && System.currentTimeMillis() - mintedAt < TOKEN_REFRESH_MILLIS) {
            return cached
        }

        try {
            InternalTokenRequest tokenRequest = new InternalTokenRequest(
                    subject: propertiesProvider.getProperty(CLIENT_ID_PROPERTY),
                    tenantId: tenantId)
            String token = secureHttpClient.post(INTERNAL_TOKEN_URL, gson.toJson(tokenRequest))
            if (!token) {
                log.warn("Auth service returned no internal token; the event will be published without elevation")
                return null
            }
            tokensByTenant.put(key, token)
            tokenMintedAt.put(key, System.currentTimeMillis())
            return token
        } catch (Exception e) {
            log.warn("Unable to mint an internal token; the event will be published without elevation", e)
            return null
        }
    }

    @Override
    List<ElevatedTopic> list() {
        return repository.list()
    }

    @Override
    ElevatedTopic create(ElevatedTopic elevatedTopic) {
        ElevatedTopic created = repository.create(elevatedTopic)
        invalidateCachedTopics()
        return created
    }

    @Override
    ElevatedTopic delete(String id) {
        ElevatedTopic deleted = repository.delete(id)
        invalidateCachedTopics()
        return deleted
    }

    private Set<String> loadElevatedTopicNames() {
        Set<String> current = cachedTopics
        if (current != null && System.currentTimeMillis() - topicsCachedAt < ALLOWLIST_TTL_MILLIS) {
            return current
        }

        try {
            Set<String> names = repository.list().findAll { it.enabled }.collect { it.topic }.findAll { it } as Set
            cachedTopics = names
            topicsCachedAt = System.currentTimeMillis()
            return names
        } catch (Exception e) {
            log.warn("Unable to read the elevated topic allowlist; events will be published without elevation", e)
            return Collections.emptySet()
        }
    }

    private void invalidateCachedTopics() {
        cachedTopics = null
        topicsCachedAt = 0
    }
}
