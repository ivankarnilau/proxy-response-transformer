package org.wiremock.extensions.template;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.wiremock.extensions.template.extensions.StubResponseTransformerWithParams;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class Main {

    public static void main(String[] args) {
        WireMockServer wireMockServer = new WireMockServer(
            options()
                .port(8089)
                .extensions(new StubResponseTransformerWithParams())
                );
        wireMockServer.start();
    }
}
