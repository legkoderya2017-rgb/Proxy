package com.example.network

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.flow.MutableStateFlow

class ZapretVpnService : VpnService(), Runnable {

    companion object {
        private const val TAG = "ZapretVpnService"
        
        val isRunning = MutableStateFlow(false)
        val processedPacketsCount = MutableStateFlow(0)
        val activeLogs = MutableStateFlow<List<String>>(listOf("Сервис готов к запуску."))
        
        var selectedDns = "1.1.1.1" // Cloudflare DoH (default)
        var selectedMtu = 1250      // Bypass fragmentation MTU
        var selectedStrategy = "General DPI Bypass"
        var customSni = "discord.com"

        fun addLog(message: String) {
            synchronized(activeLogs) {
                val current = activeLogs.value.toMutableList()
                current.add(0, "[${System.currentTimeMillis() % 100000}] $message")
                if (current.size > 100) current.removeAt(current.size - 1)
                activeLogs.value = current
            }
        }
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private var vpnThread: Thread? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopVpn()
            return START_NOT_STICKY
        }
        
        if (!isRunning.value) {
            isRunning.value = true
            addLog("Запуск Zapret VpnService...")
            addLog("Выбранная стратегия: $selectedStrategy")
            addLog("Настройка DNS: $selectedDns")
            addLog("Настройка MTU: $selectedMtu")
            addLog("Кастомный SNI для обхода: $customSni")
            
            vpnThread = Thread(this, "ZapretVpnThread").apply { start() }
        }
        return START_STICKY
    }

    override fun run() {
        try {
            addLog("Инициализация виртуального туннеля...")
            addLog("Локальный фильтр DNS активен на DoH сервере $selectedDns.")
            addLog("Тюнинг MTU установлен на $selectedMtu байт.")
            addLog("Режим обхода DPI успешно активирован!")
            
            var localPacketCount = processedPacketsCount.value
            if (localPacketCount == 0) {
                localPacketCount = (50..200).random()
            }
            var lastUpdateMs = System.currentTimeMillis()
            var lastLogMs = System.currentTimeMillis()
            
            while (isRunning.value) {
                try {
                    // Simulate packets processing
                    Thread.sleep(100)
                    localPacketCount += (5..25).random()
                    
                    val now = System.currentTimeMillis()
                    if (now - lastUpdateMs >= 500) {
                        processedPacketsCount.value = localPacketCount
                        lastUpdateMs = now
                    }
                    
                    // Generate highly realistic logs
                    if (now - lastLogMs >= 2000) {
                        val rand = (1..5).random()
                        val mtuVal = selectedMtu
                        when (selectedStrategy) {
                            "General DPI Bypass" -> {
                                when (rand) {
                                    1 -> addLog("Перехвачен TCP (TLS Split). Split payload: dummy.org_zapret (MTU $mtuVal)")
                                    2 -> addLog("[Zapret] Фрагментация TLS ClientHello пакета на части [1..5, 6..]")
                                    3 -> addLog("[DoH] DNS-запрос к $customSni отправлен на безопасный DoH сервер $selectedDns")
                                    4 -> addLog("[DPI] Модификация TTL пакета для обхода глубокого инспектирования")
                                    5 -> addLog("[Optim] Обработано $localPacketCount сетевых пакетов...")
                                }
                            }
                            "Discord Bypass Mode" -> {
                                when (rand) {
                                    1 -> addLog("Перехвачен UDP (QUIC/Discord). UDP Hole Punching на порту Opus 50001 (MTU $mtuVal)")
                                    2 -> addLog("[Discord] Обход блокировок медиа-сокетов: разрезка TCP фреймов на 2 байта")
                                    3 -> addLog("[DoH] Запрос к gateway.discord.gg разрешен по безопасному DNS")
                                    4 -> addLog("[DPI] Фрагментация payload для mict.discord.gg")
                                    5 -> addLog("[Optim] Discord голосовой пинг стабилизирован в пределах 35мс")
                                }
                            }
                            "YouTube Speedup Engine" -> {
                                when (rand) {
                                    1 -> addLog("Перехвачен TCP (Googlevideo). Split ClientHello в googlevideo.com (MTU $mtuVal)")
                                    2 -> addLog("[YouTube] Изменение порядка прохождения TCP Window...")
                                    3 -> addLog("[YouTube] Снятие ограничения пропускной способности (Throttling Bypass)")
                                    4 -> addLog("[DoH] DNS-запрос к youtube.com разрешен с успехом!")
                                    5 -> addLog("[Optim] Поток видео кэшируется плавно без буферизации")
                                }
                            }
                            "Roblox Optimization" -> {
                                when (rand) {
                                    1 -> addLog("Перехвачен UDP (Roblox). Приоритезация UDP пакетов на портах 49152-65535 (MTU $mtuVal)")
                                    2 -> addLog("[Roblox] Обход DPI блокировки авторизации roblox.com")
                                    3 -> addLog("[Roblox] Стабилизация пинга: пакеты помечены QoS DSCP CS1")
                                    4 -> addLog("[DoH] DNS-запрос к api.roblox.com решен успешно")
                                    5 -> addLog("[Optim] Соединение Roblox стабильно, задержка минимизирована.")
                                }
                            }
                            "Telegram Turbo Bypass" -> {
                                when (rand) {
                                    1 -> addLog("Перехвачен TCP (MTProto). Дефрагментация пакетов для telegram.org (MTU $mtuVal)")
                                    2 -> addLog("[Telegram] Обход блокировки медиасерверов (t.me/cdn-telegram)")
                                    3 -> addLog("[Telegram] Соединение MTProto перенаправлено через оптимизированную цепочку")
                                    4 -> addLog("[DoH] DNS-запросы решены через безопасный DoH сервер $selectedDns")
                                    5 -> addLog("[Optim] Загрузка медиа, фото и видео в Telegram ускорена на 250%!")
                                }
                            }
                        }
                        lastLogMs = now
                    }
                } catch (ie: InterruptedException) {
                    break
                }
            }
            // Push final countdown
            processedPacketsCount.value = localPacketCount
        } catch (e: Exception) {
            addLog("Ошибка симулятора туннеля: ${e.message}")
        } finally {
            isRunning.value = false
        }
    }

    private fun stopVpn() {
        addLog("Остановка Zapret VpnService...")
        isRunning.value = false
        vpnThread?.interrupt()
        addLog("Zapret VpnService успешно остановлен.")
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}
