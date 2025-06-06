package dstu.mkis44.nabokov.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

  @Bean
  fun mappingJackson2HttpMessageConverter(objectMapper: ObjectMapper ): MappingJackson2HttpMessageConverter {
    val converter = MappingJackson2HttpMessageConverter(objectMapper)
    // Добавляем поддержку application/octet-stream для JSON
    converter.supportedMediaTypes = converter.supportedMediaTypes.toMutableList().apply {
      add(MediaType.APPLICATION_OCTET_STREAM)
    }
    return converter
  }
}
