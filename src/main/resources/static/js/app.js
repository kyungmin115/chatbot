let conversationHistory = [];

async function sendMessage() {
    const input = document.getElementById('user-input');
    const message = input.value.trim();

    if (!message) return;

    // 사용자 메시지 표시
    addMessage(message, 'user');
    input.value = '';

    // 전송 버튼 비활성화
    const sendButton = document.getElementById('send-button');
    sendButton.disabled = true;
    sendButton.textContent = '전송 중...';

    try {
        const response = await fetch('/api/chat/message', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                message: message,
                history: conversationHistory
            })
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();

        // 위기 상황 알림
        if (data.crisisDetected) {
            document.getElementById('crisis-alert').style.display = 'block';
            setTimeout(() => {
                document.getElementById('crisis-alert').style.display = 'none';
            }, 10000);
        }

        // 감정 표시
        if (data.emotion) {
            const emotionDisplay = document.getElementById('emotion-display');
            emotionDisplay.textContent = '현재 감지된 감정: ' + data.emotion;
            emotionDisplay.style.display = 'block';
        }

        // AI 응답 표시
        addMessage(data.response, 'bot');

        // 대화 히스토리 업데이트
        conversationHistory.push({role: 'user', content: message});
        conversationHistory.push({role: 'assistant', content: data.response});

    } catch (error) {
        console.error('Error:', error);
        addMessage('죄송합니다. 오류가 발생했습니다. 잠시 후 다시 시도해주세요.', 'bot');
    } finally {
        sendButton.disabled = false;
        sendButton.textContent = '전송';
    }
}

function addMessage(text, sender) {
    const messagesDiv = document.getElementById('messages');

    if (!messagesDiv) {
        console.error('Error: messages element not found!');
        alert('채팅 영역을 찾을 수 없습니다. 페이지를 새로고침해주세요.');
        return;
    }

    const messageDiv = document.createElement('div');
    messageDiv.className = 'message ' + sender + '-message';

    const contentDiv = document.createElement('div');
    contentDiv.className = 'message-content';
    contentDiv.textContent = text;

    messageDiv.appendChild(contentDiv);
    messagesDiv.appendChild(messageDiv);

    const chatContainer = document.getElementById('chat-container');
    if (chatContainer) {
        chatContainer.scrollTop = chatContainer.scrollHeight;
    }
}

const userInput = document.getElementById('user-input');
if (userInput) {
    userInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });
}

window.addEventListener('load', function() {
    const input = document.getElementById('user-input');
    if (input) {
        input.focus();
    }
});