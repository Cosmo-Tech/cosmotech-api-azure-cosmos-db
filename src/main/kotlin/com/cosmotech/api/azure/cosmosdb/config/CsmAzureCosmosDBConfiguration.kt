// Copyright (c) Cosmo Tech.
// Licensed under the MIT license.
package com.cosmotech.api.azure.cosmosdb.config

import com.azure.cosmos.CosmosClient
import com.azure.cosmos.CosmosClientBuilder
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(
    name = ["csm.platform.cosmosdb.enable"], havingValue = "true", matchIfMissing = false)
internal open class CsmAzureCosmosDBConfiguration(
    private val cosmosClientBuilder: CosmosClientBuilder
) {
  @Bean open fun cosmosClient(): CosmosClient = cosmosClientBuilder.buildClient()
}
