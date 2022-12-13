package bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatAction
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.logging.LogLevel
import data.remote.API_KEY
import data.remote.repository.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.logging.Filter

private const val GIF_WAITING_URL = "https://tenor.com/view/why-am-i-still-waiting-patiently-waiting-waiting-gif-12710222"
private const val BOT_ANSWER_TIMEOUT = 30
private const val BOT_TOKEN = "5988748012:AAEa0dhFLJUJXEc8L969AmdyomSmcyX78UQ"

// 1. работа с репозиторием
class WeatherBot(private val weatherRepository: WeatherRepository) {

    private lateinit var country: String
    private var  _chatId: ChatId? = null
    private val chatId by lazy { requireNotNull(_chatId) }//Либо возвращает значение либо выбрасывает Exceptions

    fun createBot(): Bot { // функция которая создает бота
        return bot {
            timeout = BOT_ANSWER_TIMEOUT
            token = BOT_TOKEN
            logLevel = LogLevel.Error

            dispatch { // внутри этой функции мы будем отвечать полтзователю
                //text {  } // будем ждать сообщение от пользователя
                setUpCommands()
                setUpCallbacks()
            }
        }
    }
// функция которая будет обрабатывать нажатие на кнопки
    private fun Dispatcher.setUpCallbacks() {
        callbackQuery (callbackData = "getMyLocation") {
            bot.sendMessage(chatId = chatId, text = "Отправь мне свою локацию")
            location { //обрабатывает локацию
                CoroutineScope(Dispatchers.IO).launch {
                    val userCountryName = weatherRepository.getReversedGeocodingCountryName(
                        latitude = location.latitude.toString(),
                        longitude = location.longitude.toString(),
                        format = "json"
                    ).address.state

                    val inlineKeyboardMarkup = InlineKeyboardMarkup.create(// создаю кнопки в телеграме для ответа
                        listOf(
                            InlineKeyboardButton.CallbackData(
                                text = "Да, верно",
                                callbackData = "yes_label"
                            )
                        )
                    )
                    country = userCountryName

                    bot.sendMessage(
                        chatId = chatId,
                        text = "Твой город - ${country}, верно? \n Если не верно введите свой город еще раз /weather",
                        replyMarkup = inlineKeyboardMarkup  // /n возможно работает как else
                    )
                }
            }
        }
    callbackQuery(callbackData = "enterManually") {
        bot.sendMessage(chatId = chatId, text = "Хорошо, введи свой город")
        message(com.github.kotlintelegrambot.extensions.filters.Filter.Text){
            val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Да, верно.",
                        callbackData = "yes_label"
                    )
                )
            )
            country = message.text.toString()
            bot.sendMessage(
                chatId = chatId,
                text = "Твой город - ${message.text}, верно? \n Если неверно, введи свой город ещё раз.",
                replyMarkup = inlineKeyboardMarkup
            )
        }
    }
    callbackQuery(callbackData = "getWeather") {
        bot.sendMessage(chatId=chatId, text = "Нужно вывести сюда рандомную погоду от -20 до + 35")
    }
    callbackQuery(callbackData = "yes_label") {
        bot.apply {
            sendAnimation(chatId=chatId, animation = TelegramFile.ByUrl(GIF_WAITING_URL))
            sendMessage(chatId = chatId, text = "Узнаем вашу погоду...")
            sendChatAction(chatId = chatId, action = ChatAction.TYPING)//что бы видеть статус(Печатает....)
        }
         //процесс реального узнавания погоды
        CoroutineScope(Dispatchers.IO).launch {// Coroutine как запуск сервера
            val currentWeather = weatherRepository.getCurrentWeather(
                apiKey = API_KEY,
                countryName = country, // выбираю страну
                airQualityData = "no"
            )
            bot.sendMessage(
                chatId=chatId,
                //"""""" - между 3 ставишь ENTER вызывается .trimIndent() и можно вмещать большое кол-во текста
                text = """     
                    ☁️ Облачность: ${currentWeather.clouds}
                    🌡 Температура (градусы): ${currentWeather.main.temp}
                    🙎 ‍Ощущается как: ${currentWeather.main.feels_like}
                    💧 Влажность: ${currentWeather.main.humidity}
                    🌪 Скорость ветра: ${currentWeather.wind.speed}
                    🧭 Давление: ${currentWeather.main.pressure}
                """.trimIndent()
            )

            bot.sendMessage(
                chatId=chatId,
                text ="Если вы хотите запросить погоду еще раз, \n воспользуйтесь командой /weather"
            )
            country = ""
        }
    }
}

    private fun Dispatcher.setUpCommands() { //для того что бы бот мог ответить нужен ЧАТ АЙДИ

        // когда пользователь введет команду start будет выполняться тело функции
        command("start") {
            _chatId = ChatId.fromId(message.chat.id)
            bot.sendMessage(
                chatId = chatId,
                text = "Привет, я бот умеющий отправлять погоду! \n Для запуска бота введите команду /weather"
            )

        }

        command("weather") {
            //создаю кнопочки в тг
            val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Определить мой город",
                        callbackData = "getMyLocation"
                    )
                ),
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Ввести город вручную",
                        callbackData = "enterManually"
                    )
                )
            )
            bot.sendMessage(
                chatId = chatId,
                text = "Для того что бы я смог определить погоду, \n мне нужно знать твой город",
                replyMarkup = inlineKeyboardMarkup
            )
        }
    }
}