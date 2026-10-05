package com.example.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AiMunEngine
import com.example.data.local.DebateSessionEntity
import com.example.data.local.DiplomatDatabase
import com.example.data.local.DiplomatRepository
import com.example.data.local.UserEntity
import com.example.data.model.AgeRubricHelper
import com.example.data.model.Committee
import com.example.data.model.Country
import com.example.data.model.DebatePhase
import com.example.data.model.EvaluationResult
import com.example.data.model.MotionType
import com.example.data.model.MunMotion
import com.example.data.model.MunSpeech
import com.example.data.model.SpeakerRole
import com.example.data.model.StandardCommittees
import com.example.data.model.StandardCountries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class DiplomatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DiplomatRepository
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var speechTimer: CountDownTimer? = null

    // UI state
    val activeUser: StateFlow<UserEntity?>
    val allSessions: StateFlow<List<DebateSessionEntity>>

    // Chamber State
    private val _currentCommittee = MutableStateFlow<Committee>(StandardCommittees.ALL.first())
    val currentCommittee: StateFlow<Committee> = _currentCommittee.asStateFlow()

    private val _currentTopic = MutableStateFlow<String>(StandardCommittees.ALL.first().agendaOptions.first())
    val currentTopic: StateFlow<String> = _currentTopic.asStateFlow()

    private val _userCountry = MutableStateFlow<Country>(StandardCountries.ALL.first())
    val userCountry: StateFlow<Country> = _userCountry.asStateFlow()

    private val _debatePhase = MutableStateFlow<DebatePhase>(DebatePhase.ROLL_CALL)
    val debatePhase: StateFlow<DebatePhase> = _debatePhase.asStateFlow()

    private val _rollCallChoice = MutableStateFlow<String?>(null) // "PRESENT" or "PRESENT_AND_VOTING"
    val rollCallChoice: StateFlow<String?> = _rollCallChoice.asStateFlow()

    private val _speeches = MutableStateFlow<List<MunSpeech>>(emptyList())
    val speeches: StateFlow<List<MunSpeech>> = _speeches.asStateFlow()

    private val _currentSpeaker = MutableStateFlow<Country?>(null)
    val currentSpeaker: StateFlow<Country?> = _currentSpeaker.asStateFlow()

    private val _isUserTurn = MutableStateFlow<Boolean>(false)
    val isUserTurn: StateFlow<Boolean> = _isUserTurn.asStateFlow()

    // Timer State
    private val _timerTotalSeconds = MutableStateFlow<Int>(90)
    val timerTotalSeconds: StateFlow<Int> = _timerTotalSeconds.asStateFlow()

    private val _timerRemainingSeconds = MutableStateFlow<Int>(90)
    val timerRemainingSeconds: StateFlow<Int> = _timerRemainingSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow<Boolean>(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    // Voice & Audio
    private val _isListening = MutableStateFlow<Boolean>(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isTtsEnabled = MutableStateFlow<Boolean>(true)
    val isTtsEnabled: StateFlow<Boolean> = _isTtsEnabled.asStateFlow()

    private val _speechInputDraft = MutableStateFlow<String>("")
    val speechInputDraft: StateFlow<String> = _speechInputDraft.asStateFlow()

    // Active Motion & Voting
    private val _activeMotion = MutableStateFlow<MunMotion?>(null)
    val activeMotion: StateFlow<MunMotion?> = _activeMotion.asStateFlow()

    private val _userVoteChoice = MutableStateFlow<String?>(null)
    val userVoteChoice: StateFlow<String?> = _userVoteChoice.asStateFlow()

    // Evaluation
    private val _isEvaluating = MutableStateFlow<Boolean>(false)
    val isEvaluating: StateFlow<Boolean> = _isEvaluating.asStateFlow()

    private val _currentEvaluation = MutableStateFlow<EvaluationResult?>(null)
    val currentEvaluation: StateFlow<EvaluationResult?> = _currentEvaluation.asStateFlow()

    private var raisedMotionsCount = 0

    init {
        val db = DiplomatDatabase.getDatabase(application)
        repository = DiplomatRepository(db.diplomatDao())

        activeUser = repository.activeUser.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        allSessions = repository.allSessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        initTextToSpeech(application)
    }

    private fun initTextToSpeech(app: Application) {
        textToSpeech = TextToSpeech(app) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
            }
        }
    }

    fun toggleTts() {
        _isTtsEnabled.value = !_isTtsEnabled.value
        if (!_isTtsEnabled.value) {
            textToSpeech?.stop()
        }
    }

    fun speak(text: String) {
        if (_isTtsEnabled.value) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "mun_tts_${System.currentTimeMillis()}")
        }
    }

    // User Profile Management (Login / Sign Up)
    fun registerOrLoginUser(name: String, email: String, age: Int, preferredCountry: String = "USA") {
        viewModelScope.launch(Dispatchers.IO) {
            val user = UserEntity(
                name = name.trim().ifBlank { "Diplomat Candidate" },
                email = email.trim(),
                age = age.coerceIn(10, 99),
                preferredCountryCode = preferredCountry,
                title = "Delegate of $preferredCountry"
            )
            repository.saveUser(user)
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearSession()
        }
    }

    // Conference Setup
    fun setupConference(committee: Committee, topic: String, userCountry: Country, speechTimeSeconds: Int) {
        _currentCommittee.value = committee
        _currentTopic.value = topic
        _userCountry.value = userCountry
        _timerTotalSeconds.value = speechTimeSeconds
        _timerRemainingSeconds.value = speechTimeSeconds
        _debatePhase.value = DebatePhase.ROLL_CALL
        _rollCallChoice.value = null
        _speeches.value = emptyList()
        _currentSpeaker.value = null
        _isUserTurn.value = false
        _activeMotion.value = null
        _userVoteChoice.value = null
        _currentEvaluation.value = null
        raisedMotionsCount = 0

        // Chair Roll Call announcement
        val chairIntro = "The Dais calls the ${committee.name} to order. The agenda before the committee is \"$topic\". We shall proceed with the formal Roll Call. When your delegation is called, please answer 'Present' or 'Present and Voting'."
        addChairSpeech(chairIntro)
        speak(chairIntro)
    }

    // Roll Call
    fun answerRollCall(choice: String) {
        _rollCallChoice.value = choice
        val user = _userCountry.value
        val responseText = if (choice == "PRESENT_AND_VOTING") {
            "The delegation of ${user.name} is Present and Voting, Honorable Chair."
        } else {
            "The delegation of ${user.name} is Present, Honorable Chair."
        }

        addUserSpeech(responseText)

        // Chair transitions to GSL
        viewModelScope.launch {
            val openingChairRemarks = AiMunEngine.generateChairpersonIntro(_currentCommittee.value, _currentTopic.value)
            addChairSpeech(openingChairRemarks)
            speak(openingChairRemarks)

            _debatePhase.value = DebatePhase.GENERAL_SPEAKERS_LIST
            // Call on first speaker
            recognizeNextSpeaker()
        }
    }

    // GSL & Speaker Flow
    fun recognizeNextSpeaker() {
        stopTimer()
        val committeeCountries = StandardCountries.ALL.filter { it.code != _userCountry.value.code }.shuffled()
        
        // Randomly alternate between AI delegates and User
        val shouldRecognizeUser = _speeches.value.count { it.speakerRole == SpeakerRole.USER } < 2 || (0..1).random() == 0

        if (shouldRecognizeUser && !_isUserTurn.value) {
            _isUserTurn.value = true
            _currentSpeaker.value = _userCountry.value
            val chairCall = "The Chair now recognizes the distinguished delegate of ${_userCountry.value.name} for ${_timerTotalSeconds.value} seconds. The delegate has the floor."
            addChairSpeech(chairCall)
            speak(chairCall)
            startTimer(_timerTotalSeconds.value)
        } else {
            _isUserTurn.value = false
            val aiCountry = committeeCountries.first()
            _currentSpeaker.value = aiCountry
            val chairCall = "The Chair recognizes the distinguished delegate of ${aiCountry.name} for ${_timerTotalSeconds.value} seconds."
            addChairSpeech(chairCall)
            speak(chairCall)
            startTimer(_timerTotalSeconds.value)

            // Generate AI Delegate Speech
            viewModelScope.launch {
                val speech = AiMunEngine.generateDelegateSpeech(
                    speakerCountry = aiCountry,
                    userCountry = _userCountry.value,
                    committee = _currentCommittee.value,
                    topic = _currentTopic.value,
                    lastSpeeches = _speeches.value,
                    phase = _debatePhase.value
                )
                addDelegateSpeech(aiCountry, speech)
                speak(speech)
            }
        }
    }

    fun submitUserSpeech(speechText: String) {
        val trimmed = speechText.trim()
        if (trimmed.isBlank()) return

        addUserSpeech(trimmed)
        _speechInputDraft.value = ""
        stopTimer()

        // Chair acknowledges and gavel strikes
        vibrateDevice(80)
        val chairRuling = "The Chair thanks the delegate of ${_userCountry.value.name}. The floor yields back to the Dais."
        addChairSpeech(chairRuling)
        speak(chairRuling)

        _isUserTurn.value = false
        // Next delegate
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            recognizeNextSpeaker()
        }
    }

    // Points & Motions
    fun proposeMotion(
        type: MotionType,
        topic: String = "",
        totalMinutes: Int = 10,
        speakingSeconds: Int = 60
    ) {
        raisedMotionsCount++
        val motion = MunMotion(
            id = "motion_${System.currentTimeMillis()}",
            type = type,
            proposedByCountryCode = _userCountry.value.code,
            topic = topic.ifBlank { _currentTopic.value },
            totalMinutes = totalMinutes,
            speakingSeconds = speakingSeconds
        )

        when (type) {
            MotionType.POINT_OF_ORDER,
            MotionType.POINT_OF_PARLIAMENTARY_INQUIRY,
            MotionType.POINT_OF_PERSONAL_PRIVILEGE -> {
                // Procedural points handled directly by Chair
                val pointText = "The delegate of ${_userCountry.value.name} rises to a ${type.title}: \"$topic\""
                addUserSpeech(pointText)
                val ruling = when (type) {
                    MotionType.POINT_OF_ORDER -> "Point of Order is recognized. The Dais clarifies that proceedings remain strictly aligned with UN Rules of Procedure."
                    MotionType.POINT_OF_PARLIAMENTARY_INQUIRY -> "The Dais notes the inquiry. Delegates may yield time to the Chair or to questions at the conclusion of speech."
                    else -> "Point of Personal Privilege is noted and granted by the Chair."
                }
                addChairSpeech(ruling)
                speak(ruling)
            }
            MotionType.MODERATED_CAUCUS,
            MotionType.UNMODERATED_CAUCUS,
            MotionType.INTRODUCE_DRAFT_RESOLUTION -> {
                // Requires committee vote
                _activeMotion.value = motion
                val motionText = "The delegate of ${_userCountry.value.name} moves for a ${type.title} on \"${motion.topic}\" for a total time of ${motion.totalMinutes} minutes and ${motion.speakingSeconds} seconds individual speaking time."
                addUserSpeech(motionText)

                val chairVoteCall = "The motion is in order. We shall now move directly into a procedural vote on the motion for a ${type.title}. All those in favor, raise your placards."
                addChairSpeech(chairVoteCall)
                speak(chairVoteCall)

                // Simulate committee voting
                simulateCommitteeVotes(motion)
            }
        }
    }

    private fun simulateCommitteeVotes(motion: MunMotion) {
        val totalMembers = 15
        val favorVotes = (8..13).random()
        val againstVotes = totalMembers - favorVotes

        motion.votesInFavor = favorVotes
        motion.votesAgainst = againstVotes
        motion.passed = favorVotes >= 8
    }

    fun castUserVote(choice: String) {
        _userVoteChoice.value = choice
        val motion = _activeMotion.value ?: return

        if (choice == "FAVOR") motion.votesInFavor++
        else if (choice == "AGAINST") motion.votesAgainst++

        val passed = motion.votesInFavor >= 8
        motion.passed = passed

        val chairOutcome = if (passed) {
            "With ${motion.votesInFavor} votes in favor, the motion passes. The committee now moves into a ${motion.type.title}."
        } else {
            "With ${motion.votesInFavor} votes in favor and ${motion.votesAgainst} against, the motion fails. Debate resumes on the General Speakers' List."
        }

        addChairSpeech(chairOutcome)
        speak(chairOutcome)

        if (passed && motion.type == MotionType.MODERATED_CAUCUS) {
            _debatePhase.value = DebatePhase.MODERATED_CAUCUS
            _timerTotalSeconds.value = motion.speakingSeconds
            _timerRemainingSeconds.value = motion.speakingSeconds
        }

        _activeMotion.value = null
        _userVoteChoice.value = null
    }

    // Yields
    fun yieldToChair() {
        val yieldText = "The delegate yields the remainder of their time to the Chair."
        addUserSpeech(yieldText)
        stopTimer()
        val chairAcknowledge = "The Chair accepts the remaining time. The floor is yielded."
        addChairSpeech(chairAcknowledge)
        speak(chairAcknowledge)
        _isUserTurn.value = false
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            recognizeNextSpeaker()
        }
    }

    fun yieldToQuestions() {
        val yieldText = "The delegate yields the remaining time to Points of Information or questions from the floor."
        addUserSpeech(yieldText)
        val chairCall = "The delegate has yielded to questions. Are there any points of information from the floor?"
        addChairSpeech(chairCall)
        speak(chairCall)

        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            val questioner = StandardCountries.ALL.filter { it.code != _userCountry.value.code }.random()
            val poi = "The delegate of ${questioner.name} inquires: What enforcement mechanism does your delegation propose to prevent non-compliance?"
            addDelegateSpeech(questioner, poi)
            speak(poi)
        }
    }

    // Speech Timer Controls
    fun toggleTimer() {
        if (_isTimerRunning.value) {
            pauseTimer()
        } else {
            resumeTimer()
        }
    }

    private fun startTimer(seconds: Int) {
        speechTimer?.cancel()
        _timerRemainingSeconds.value = seconds
        _isTimerRunning.value = true

        speechTimer = object : CountDownTimer((seconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val remaining = (millisUntilFinished / 1000).toInt()
                _timerRemainingSeconds.value = remaining
                if (remaining == 15 || remaining == 5) {
                    vibrateDevice(50)
                }
            }

            override fun onFinish() {
                _timerRemainingSeconds.value = 0
                _isTimerRunning.value = false
                vibrateDevice(200)
                val timeCall = "Gavel! Time has elapsed. Delegate, please yield the floor."
                addChairSpeech(timeCall)
                speak(timeCall)
            }
        }.start()
    }

    fun pauseTimer() {
        speechTimer?.cancel()
        _isTimerRunning.value = false
    }

    fun resumeTimer() {
        if (_timerRemainingSeconds.value > 0) {
            startTimer(_timerRemainingSeconds.value)
        }
    }

    fun resetTimer() {
        stopTimer()
        _timerRemainingSeconds.value = _timerTotalSeconds.value
    }

    fun addTimerSeconds(seconds: Int) {
        val newTime = _timerRemainingSeconds.value + seconds
        _timerRemainingSeconds.value = newTime
        if (_isTimerRunning.value) {
            startTimer(newTime)
        }
    }

    private fun stopTimer() {
        speechTimer?.cancel()
        _isTimerRunning.value = false
    }

    // Voice Dictation
    fun setSpeechInputDraft(text: String) {
        _speechInputDraft.value = text
    }

    fun toggleVoiceInput() {
        if (_isListening.value) {
            stopVoiceInput()
        } else {
            startVoiceInput()
        }
    }

    private fun startVoiceInput() {
        val app = getApplication<Application>()
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(app)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { _isListening.value = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { _isListening.value = false }
            override fun onError(error: Int) {
                _isListening.value = false
                Log.d("DiplomatViewModel", "Speech recognition code: $error")
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spoken = matches[0]
                    _speechInputDraft.value = if (_speechInputDraft.value.isBlank()) spoken else "${_speechInputDraft.value} $spoken"
                }
                _isListening.value = false
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val partial = matches[0]
                    _speechInputDraft.value = partial
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            _isListening.value = false
        }
    }

    private fun stopVoiceInput() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {}
        _isListening.value = false
    }

    // End Debate & Evaluate
    fun concludeDebateAndEvaluate() {
        stopTimer()
        _isEvaluating.value = true

        val user = activeUser.value
        val age = user?.age ?: 17
        val userName = user?.name ?: "Delegate"

        val userSpeeches = _speeches.value.filter { it.speakerRole == SpeakerRole.USER }

        viewModelScope.launch(Dispatchers.IO) {
            val eval = AiMunEngine.evaluateDelegatePerformance(
                userAge = age,
                userName = userName,
                userCountry = _userCountry.value,
                committee = _currentCommittee.value,
                topic = _currentTopic.value,
                userSpeeches = userSpeeches,
                totalConferenceSpeeches = _speeches.value.size,
                raisedMotionsCount = raisedMotionsCount
            )

            _currentEvaluation.value = eval
            _isEvaluating.value = false

            // Save to Room DB
            val sessionEntity = DebateSessionEntity(
                userId = user?.id ?: 1,
                committeeAcronym = _currentCommittee.value.acronym,
                topic = _currentTopic.value,
                countryCode = _userCountry.value.code,
                dateEpoch = System.currentTimeMillis(),
                overallScore = eval.overallScore,
                awardTier = eval.awardTier,
                evaluationJson = serializeEvaluation(eval),
                speechesCount = userSpeeches.size
            )
            repository.saveSession(sessionEntity)

            // Update user stats
            if (user != null) {
                val updatedTotal = user.totalDebates + 1
                val updatedAvg = if (user.totalDebates == 0) eval.overallScore.toDouble() else ((user.averageScore * user.totalDebates) + eval.overallScore) / updatedTotal
                val updatedAward = if (eval.overallScore >= 90) eval.awardTier else user.highestAward
                repository.updateUser(
                    user.copy(
                        totalDebates = updatedTotal,
                        averageScore = updatedAvg,
                        highestAward = updatedAward
                    )
                )
            }
        }
    }

    private fun serializeEvaluation(eval: EvaluationResult): String {
        val obj = JSONObject()
        obj.put("overallScore", eval.overallScore)
        obj.put("userAge", eval.userAge)
        obj.put("ageBracket", eval.ageBracket)
        obj.put("ageRubricContext", eval.ageRubricContext)
        obj.put("awardTier", eval.awardTier)
        obj.put("awardIcon", eval.awardIcon)
        obj.put("substantivePolicyScore", eval.substantivePolicyScore)
        obj.put("parliamentaryScore", eval.parliamentaryScore)
        obj.put("rhetoricScore", eval.rhetoricScore)
        obj.put("timeManagementScore", eval.timeManagementScore)
        obj.put("strategicConsensusScore", eval.strategicConsensusScore)
        obj.put("chairSummaryRemarks", eval.chairSummaryRemarks)

        val sArray = JSONArray()
        eval.strengths.forEach { sArray.put(it) }
        obj.put("strengths", sArray)

        val iArray = JSONArray()
        eval.areasForImprovement.forEach { iArray.put(it) }
        obj.put("areasForImprovement", iArray)

        return obj.toString()
    }

    fun parseEvaluation(jsonStr: String): EvaluationResult? {
        return try {
            val obj = JSONObject(jsonStr)
            val sArray = obj.optJSONArray("strengths")
            val strengths = mutableListOf<String>()
            if (sArray != null) {
                for (i in 0 until sArray.length()) strengths.add(sArray.getString(i))
            }
            val iArray = obj.optJSONArray("areasForImprovement")
            val improvements = mutableListOf<String>()
            if (iArray != null) {
                for (i in 0 until iArray.length()) improvements.add(iArray.getString(i))
            }

            EvaluationResult(
                overallScore = obj.getInt("overallScore"),
                userAge = obj.optInt("userAge", 17),
                ageBracket = obj.optString("ageBracket", AgeRubricHelper.getBracket(17)),
                ageRubricContext = obj.optString("ageRubricContext", ""),
                awardTier = obj.getString("awardTier"),
                awardIcon = obj.optString("awardIcon", "🏆"),
                substantivePolicyScore = obj.getInt("substantivePolicyScore"),
                parliamentaryScore = obj.getInt("parliamentaryScore"),
                rhetoricScore = obj.getInt("rhetoricScore"),
                timeManagementScore = obj.getInt("timeManagementScore"),
                strategicConsensusScore = obj.getInt("strategicConsensusScore"),
                strengths = strengths,
                areasForImprovement = improvements,
                chairSummaryRemarks = obj.getString("chairSummaryRemarks")
            )
        } catch (e: Exception) {
            null
        }
    }

    // Helpers
    private fun addChairSpeech(text: String) {
        val speech = MunSpeech(
            id = "speech_${System.currentTimeMillis()}_${(0..999).random()}",
            speakerRole = SpeakerRole.CHAIR,
            speakerName = "Chairperson (Dais)",
            text = text,
            phase = _debatePhase.value
        )
        _speeches.value = _speeches.value + speech
    }

    private fun addDelegateSpeech(country: Country, text: String) {
        val speech = MunSpeech(
            id = "speech_${System.currentTimeMillis()}_${(0..999).random()}",
            speakerRole = SpeakerRole.DELEGATE,
            speakerName = country.name,
            countryCode = country.code,
            text = text,
            phase = _debatePhase.value
        )
        _speeches.value = _speeches.value + speech
    }

    private fun addUserSpeech(text: String) {
        val speech = MunSpeech(
            id = "speech_${System.currentTimeMillis()}_${(0..999).random()}",
            speakerRole = SpeakerRole.USER,
            speakerName = "Delegate of ${_userCountry.value.name}",
            countryCode = _userCountry.value.code,
            text = text,
            phase = _debatePhase.value
        )
        _speeches.value = _speeches.value + speech
    }

    private fun vibrateDevice(durationMillis: Long) {
        try {
            val vibrator = getApplication<Application>().getSystemService(Vibrator::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMillis)
            }
        } catch (e: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        speechTimer?.cancel()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        speechRecognizer?.destroy()
    }
}
