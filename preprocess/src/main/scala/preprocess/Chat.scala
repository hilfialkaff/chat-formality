package preprocess

import java.text.SimpleDateFormat
import java.util.Date

class Chat(isSent: Boolean, chat: String) {
  def getIsSent(): Boolean = isSent
  def getChat(): String = chat
}