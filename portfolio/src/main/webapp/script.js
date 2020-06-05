// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const textContainer = document.getElementById('text-container');
  textContainer.innerText = greeting;
}

/**
 * Adds a random greeting to the page.
 */
function addRandomQuote() {
  const quotes = [
        "A ship in port is safe, but that's not what ships are built for.",
        "I don't know where we're going, but we're making good time.",
        "Sometimes you never realize the value of a moment until it becomes a memory.",
        "And now that you don't have to be perfect, you can be good.",
        "Holding a grudge is like drinking poison and expecting the other person to die."
    ];

  // Pick a random greeting.
  const quote = quotes[Math.floor(Math.random() * quotes.length)];

  // Add it to the page.
  const textContainer = document.getElementById('text-container');
  textContainer.innerText = quote;
}