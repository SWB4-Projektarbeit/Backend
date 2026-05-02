package de.hsesslingen.timesy.backend;

import de.zeanon.thunderfilemanager.internal.files.config.ThunderConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


@Service
public class HEOnlineService {

    public static final String HE_ONLINE_URL = "HE-Online URL";

    private final RestClient restClient;

    public HEOnlineService(final @NotNull ThunderConfig thunderConfig) {
        restClient = RestClient.builder()
                .baseUrl(Utils.getAndValidateUrl(thunderConfig, HE_ONLINE_URL))
                .build();
    }
}
