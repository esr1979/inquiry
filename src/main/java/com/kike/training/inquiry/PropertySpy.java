package com.kike.training.inquiry;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class PropertySpy {

    private final Environment environment;

    @Value("${spring.datasource.names:PROPIEDAD NO ENCONTRADA CON @VALUE}")
    private String namesFromValue;

    public PropertySpy(Environment environment) {
        this.environment = environment;
        System.out.println(">>>> [PropertySpy CONSTRUCTOR] El espía ha sido creado.");
    }

    @PostConstruct
    public void checkProperties() {
        System.out.println("===============================================================");
        System.out.println(">>>> [PropertySpy POSTCONSTRUCT] DIAGNÓSTICO DE PROPIEDADES <<<<");
        System.out.println("===============================================================");

        String namesFromEnv = environment.getProperty("spring.datasource.names");

        if (namesFromEnv != null && !namesFromEnv.isEmpty()) {
            System.out.println(">>>> RESULTADO (Environment): ¡ÉXITO! La propiedad 'spring.datasource.names' es: '" + namesFromEnv + "'");
        } else {
            System.out.println(">>>> RESULTADO (Environment): ¡FRACASO! environment.getProperty(\"spring.datasource.names\") es NULL o VACÍO.");
        }

        System.out.println(">>>> RESULTADO (@Value): La propiedad 'spring.datasource.names' es: '" + namesFromValue + "'");
        System.out.println("===============================================================");
    }
}
