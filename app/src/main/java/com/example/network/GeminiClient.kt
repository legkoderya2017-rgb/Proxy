package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

class GeminiClient {
    suspend fun getDiagnosticResponse(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val isKeyPlaceholder = apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY"
        
        // Execute dynamic local advice engine if the API key is not configured yet
        if (isKeyPlaceholder) {
            return@withContext getLocalFallbackResponse(prompt)
        }
        
        val systemMessage = "Ты — AI NetDoctor, эксперт в обходе сетевых блокировок (DPI), оптимизации пинга и настройки утилиты Zapret для Android. " +
                "Помогай пользователям настраивать приложение, объяснять принципы фрагментации пакетов, подмены SNI, обхода замедления YouTube, Discord, Telegram, Roblox и ИИ-сервисов (ChatGPT, Claude). " +
                "Отвечай кратко, емко, со знанием дела, в стиле гика или терминала. Давай практические советы для мобильных операторов (МТС, Билайн, Мегафон, Теле2) и домашнего Wi-Fi."

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemMessage))),
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        return@withContext try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "AI не вернул текстовый ответ."
        } catch (e: Exception) {
            val errMessage = e.localizedMessage ?: e.message ?: ""
            if (errMessage.contains("400") || errMessage.contains("403") || errMessage.contains("API key")) {
                // If the key was entered but is invalid, run fallback nicely instead of crashing
                getLocalFallbackResponse(prompt)
            } else {
                "Ошибка при запросе к ИИ: $errMessage. Пожалуйста, проверьте подключение к Интернету."
            }
        }
    }

    private fun getLocalFallbackResponse(prompt: String): String {
        val lowercasePrompt = prompt.lowercase()
        return when {
            lowercasePrompt.contains("пинг") || lowercasePrompt.contains("ping") || lowercasePrompt.contains("задержк") || lowercasePrompt.contains("медленн") || lowercasePrompt.contains("лагов") || lowercasePrompt.contains("лаг") -> {
                "🤖 **[NetDoctor Local Mode: Пинг и скорость]**\n\n" +
                "Высокий пинг (latency) или медленная загрузка на **YouTube** и **Roblox** вызваны жестким замедлением пакетов на узлах DPI (Deep Packet Inspection) интернет-провайдеров:\n\n" +
                "1. **Почему без обхода пинг огромный или пропадает связь?** Провайдер искусственно придерживает, модифицирует, либо сбрасывает TCP/UDP пакеты от серверов Google Video и игровых хабов Roblox. Это приводит к искусственной задержке (до 1500-2500 мс) и потере пакетов!\n" +
                "2. **Как это исправляет Zapret?** Наша утилита разделяет TLS ClientHello на мелкие части и изменяет значение TTL. В результате DPI-сенсоры провайдера не могут собрать пакет и распознать заблокированный ресурс, пропуская трафик напрямую на максимальной скорости и с **минимальным чистым пингом (30 - 80 мс)**!\n" +
                "3. **Рекомендации по настройке для лучшего пинга:**\n" +
                "   - Активируйте **«YouTube Speedup Engine»** для видео или **«Roblox Optimization»** для игр.\n" +
                "   - Обязательно подберите DNS во вкладке **«Настройки»** (например, AdGuard DNS блокирует рекламные запросы, снижая пинг в играх).\n" +
                "   - Установите MTU равным **1250** или **1200**. Мелкие пакеты проходят DPI-фильтры намного быстрее!"
            }
            lowercasePrompt.contains("ютуб") || lowercasePrompt.contains("youtube") || lowercasePrompt.contains("видео") -> {
                "🤖 **[NetDoctor Local Mode: YouTube]**\n\n" +
                "Для обхода замедления и блокировки **YouTube** на мобильном интернете или Wi-Fi сделайте следующее:\n" +
                "1. В главном меню переключите стратегию на **«YouTube Speedup Engine»**.\n" +
                "2. Эта стратегия настраивает пакетную нарезку HTTP/2 окон и разделение ClientHello специально для доменов `googlevideo.com`.\n" +
                "3. Установите DNS на **«Google Secure DNS» (8.8.8.8)**.\n" +
                "4. Снизьте размер MTU до **1200** или **1250** байт — это заставит алгоритмы DPI провайдера спотыкаться при склеивании фрагментов.\n" +
                "5. Запустите VPN и проверьте статус YouTube!"
            }
            lowercasePrompt.contains("дискорд") || lowercasePrompt.contains("discord") || lowercasePrompt.contains("голосовой") || lowercasePrompt.contains("звонк") -> {
                "🤖 **[NetDoctor Local Mode: Discord]**\n\n" +
                "Для разблокировки медиа-ресурсов и голосовой связи в **Discord**:\n" +
                "1. Выберите стратегию **«Discord Bypass Mode»**.\n" +
                "2. Данная стратегия включает алгоритм UDP Hole Punching для обхода блокировок голосовых шлюзов на портах Opus, а также фрагментирует TCP фреймы на мелкие части по 2 байта.\n" +
                "3. Введите в поле кастомного SNI: **gateway.discord.gg** или **discordapp.com**.\n" +
                "4. Установите размер MTU на **1220** байт (оптимально для сетей МТС и Мегафон)."
            }
            lowercasePrompt.contains("роблокс") || lowercasePrompt.contains("roblox") || lowercasePrompt.contains("игра") -> {
                "🤖 **[NetDoctor Local Mode: Roblox]**\n\n" +
                "Для оптимизации пинга и обхода зависаний в плейсах **Roblox**:\n" +
                "1. Смените стратегию на **«Roblox Optimization»**.\n" +
                "2. Установите DNS-провайдер **«AdGuard Anti-Ad DNS» (94.140.14.14)** — он отсечет фоновые рекламные трекеры и ускорит резолв игровых серверов.\n" +
                "3. Этот режим использует метку приоритезации трафика QoS DSCP CS1, чтобы игровые пакеты передавались без задержек по транспортным узлам вашего оператора.\n" +
                "4. Убедитесь, что MTU установлен в пределах **1250** байт."
            }
            lowercasePrompt.contains("телеграм") || lowercasePrompt.contains("telegram") || lowercasePrompt.contains("тг") || lowercasePrompt.contains("тгшка") || lowercasePrompt.contains("телеграмм") -> {
                "🤖 **[NetDoctor Local Mode: Telegram]**\n\n" +
                "Для ускорения загрузки фото, видео, тяжелых медиафайлов и стабильных звонков в **Telegram**:\n" +
                "1. Смените стратегию обхода на **«Telegram Turbo Bypass»**.\n" +
                "2. Установите DNS-пресет в **«Cloudflare Secure DNS» (1.1.1.1)** или **«Google Secure DNS» (8.8.8.8)**.\n" +
                "3. Эта стратегия применяет дефрагментацию MTProto-пакетов и разделение TLS-хендшейков специально для IP-диапазонов и CDN-серверов Telegram (`telegram.org`, `t.me`).\n" +
                "4. Установите размер MTU на **1250** или **1200** байт. Это позволяет пакетам проскальзывать через глубокие DPI-фильтры провайдеров без искусственных задержек, в разы ускоряя скачивание картинок и видео в каналах!"
            }
            lowercasePrompt.contains("нейросети") || lowercasePrompt.contains("gpt") || lowercasePrompt.contains("chatgpt") || lowercasePrompt.contains("claude") || lowercasePrompt.contains("ии") -> {
                "🤖 **[NetDoctor Local Mode: AI Chats]**\n\n" +
                "Для беспрепятственного доступа к ИИ-сервисам (ChatGPT, Claude):\n" +
                "1. Активируйте стратегию **«General DPI Bypass»**.\n" +
                "2. В поле кастомного SNI введите домен целевого сервиса (например, **chatgpt.com** или **claude.ai**).\n" +
                "3. Утилита Zapret подменит TLS ClientHello на фейковый SNI (`dummy.org_zapret`), а реальный зашифрованный SNI отправит в расколотых пакетах.\n" +
                "4. Используйте DNS **«Cloudflare Secure DNS» (1.1.1.1)**."
            }
            lowercasePrompt.contains("ошибка") || lowercasePrompt.contains("не работает") || lowercasePrompt.contains("почему") || lowercasePrompt.contains(" help") -> {
                "🤖 **[NetDoctor Local Mode: Диагностика проблем]**\n\n" +
                "Если обход не срабатывает:\n" +
                "1. **Обновите статус**: Перейдите в раздел «Монитор» и нажмите «Проверить пинги».\n" +
                "2. **Доступ к TUN**: Если в логах написано, что инициализация туннеля TUN отклонена, при запуске VPN обязательно предоставьте Android-разрешение на создание VPN.\n" +
                "3. **Плохой DNS**: Некоторые мобильные операторы перехватывают DNS запросы. Пробуйте разные варианты во вкладке Настройки, например, AdGuard DNS или Яндекс DNS."
            }
            else -> {
                "🤖 **[NetDoctor Local Mode]**\n\n" +
                "Привет! Я локальный помощник эксперта по сетям NetDoctor.\n\n" +
                "Задайте мне конкретный вопрос. Например:\n" +
                "• *«Как ускорить работу Telegram?»*\n" +
                "• *«Как настроить YouTube?»*\n" +
                "• *«Почему лагает Discord?»*\n" +
                "• *«Как ускорить игры и уменьшить пинг в Roblox?»*\n" +
                "• *«Какая конфигурация нужна для ChatGPT?»*\n\n" +
                "*(Вы также можете добавить свой рабочий `GEMINI_API_KEY` в панель Secrets в Google AI Studio для включения полной нейросети).* "
            }
        }
    }
}

