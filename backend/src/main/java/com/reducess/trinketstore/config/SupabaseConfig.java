package com.reducess.trinketstore.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class SupabaseConfig {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon.key}")
    private String anonKey;

    @Value("${supabase.service.role.key}")
    private String serviceRoleKey;

    @Value("${supabase.jwt.secret}")
    private String jwtSecret;
}

