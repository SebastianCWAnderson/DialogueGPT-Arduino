# DialogueGPT (Arduino Version)

## Project Description

DialogueGPT is a project that fuses together multiple different hardware and software components into one cohesive product. It uses OpenAI' GPT (_Generative Pre-Trained Transformer_) LLM (large language model) to create a 'dialogue' between the user and the LLM.


### Purpose and Benefits

DialogueGPT serves as a 'bridge' between the OpenAI GPT API and the user; utilizing both hardware and software components. The purpose of the project is to change the medium of communication with the LLM; using voice input instead of keyboard input.

The purpose of DialogueGPT is to demonstrate the capabilities of OpenAI's language model, in generating contextually relevant responses in conversational settings. By developing this project, we aim to showcase the potential applications of conversational AI and provide an interactive platform for users to engage in dialogue with an AI model.

**The main benefits of DialogueGPT are the following:**

- Streamlined User Experience
    
Using DialogueGPT is as easy as speaking into the microphone. Instead of the default mode of communication with the GPT LLM, which is using keyboard text input, you can instead use vocal input. DialogueGPT can be used by people of all ages, provided the user speaks any of the languages supported by the API, although the user interface is written in English.

- Exploration of AI Capabilities

Given the advent of generative AI models that have started to blossom during 2022 & 2023; DialogueGPT serves as a beginner-friendly introduction to AI systems that requires little-to-no prior experience when it comes to usage and understanding. There are opportunities for users to fork the repository and expand upon the orignal scope of the project; which includes potential business/monetization options. In addition to this; reading the repository source code provides useful insights into how to practically design and implement a simpler AI system. 

---

## Get Started:

### Installation:

0. Clone the GitLab repository.

#### A) Set up Mosquitto, the MQTT broker:
1. Navigate to the installation directory (By default `C:\Program Files\mosquitto` on Windows).
2. Add the following lines to `mosquitto.conf` file:
```
listener 1883
protocol mqtt
allow_anonymous true
```
3. Open CMD/PowerShell in the directory.
4. Run `mosquitto -c mosquitto.conf -v` to start the broker.

In case you get an error while trying to run the command, run `services.msc`, find `Mosquitto Broker` in the list, stop the service and change it's startup type to `Manual`.

#### B) Wio terminal setup
1. Open the `DialogueGPT.ino` file in Arduino IDE (located in folder `wio`).
2. Follow the on-screen instructions to create a new folder for the sketch.
3. In that folder, create a header file named `heading.h`. Follow this template:
```c
#define SSID "wifi name" 
#define PASSWORD "wifi password" 
#define my_IPv4 "ipv4 address" // MQTT broker IP, find it by running `ipconfig` on the host machine
```
4. Copy the header file named `notes.h` to the sketch folder.

5. Upload the sketch to Wio Terminal.

#### C) Android app setup
1. Open the `android` folder in Android Studio.
2. Press both `Sync Project with Gradle Files` as well as `Make Project` buttons.

**NOTE:** You might need to define the SDK before being able to build the project.

3. [Enable USB debugging](https://developer.android.com/studio/debug/dev-options#:~:text=Enable%20USB%20debugging%20on%20your%20device,-Before%20you%20can&text=Enable%20USB%20debugging%20in%20the,Advanced%20%3E%20Developer%20Options%20%3E%20USB%20debugging) on your physical device, otherwise use a built-in emulator, API 33.

4. Press the `Run 'app'` button to upload the application to either your physical device or an emulator.

**Optional step:** If you are using an emulator, you will need to open `Extended controls` and there, navigate to `Microphone` tab and enable all three switches.

#### D) ChatGPT API key

1. Go to [the OpenAI signup page](https://auth0.openai.com/u/signup/identifier?state=hKFo2SBqdlhsaVpNeHA2dGFtdzZSYVJWaHhwdkJhWnlydUlWeqFur3VuaXZlcnNhbC1sb2dpbqN0aWTZIHJxaVBVbGVYTnJQQ3BVS3JiZWQxQVE0dUhLVE9SbDhNo2NpZNkgRFJpdnNubTJNdTQyVDNLT3BxZHR3QjNOWXZpSFl6d0Q) and create an account.
2. After creating an account, go to your OpenAI homepage and press the ['View API Keys' button](https://imgur.com/a/Wxijvt7).
3. Press the ['Create new secret key' button](https://imgur.com/a/xhS0G5h) to create your **personal** API key (**do not share your personal API key**).
4. Input your ChatGPT API key into the android application, the input field is located in the settings.

#### DialogueGPT is now ready to be used.

---

### Glossary:

**What is a LLM?**`

A Large Language Model is a computer program that has been trained to understand and generate human-like language. It is designed to process and analyze vast amounts of text data and learn patterns, grammar and context from that information. The model can then, based on that training data, generate coherent and relevant responses to questions or prompts given to it.

**What is OpenAI?**

OpenAI is a hybrid non-profit/for-profit company based in San Francisco, California. They were founded in 2015 as an artificial intelligence research laboratory with a stated mission to ensure safe and ethical research and development of artificial intelligence systems. They've since then become a hybrid-profit motive company, with a for-profit subsidiary of OpenAI called OpenAI Partnership; with a 100x cap on any investments made into the company.

**What is the 'GPT API?'**

In March 2023, OpenAI published a research paper on their newest Large Language Model, called GPT-4. GPT is, in summary, a LLM that has been trained on a large corpus of training data, in this case, text collected and scraped from the internet. According to OpenAI, their GPT models have been trained on books, text documents, articles, websites and all other sorts of tokens (words and characters)

---

### Bias:

As an AI language model, GPT learns from vast amounts of text data from the internet, which can contain biases present in the source material. These biases can manifest in various ways, including gender, racial, and cultural biases.

For instance, if a LLM is **only** trained on text and literature written by an author who has a very strong political bias; it is probable that the LLM will reflect that bias in its outputs.

OpenAI, the company behind the GPT LLM used in this project, acknowledges the presence of biases and is actively working to mitigate them. They have made efforts to reduce both glaring and subtle biases in the system and have made ongoing research and development a priority in addressing this issue since it presents potentially harmful or negative outcomes if the models are used widespread.
---
