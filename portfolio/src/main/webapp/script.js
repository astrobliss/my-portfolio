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
 * Defines getCurrentUser.cachedUser by fetching the current user from UserInformationServlet.
 * If the user is not logged in, the fetch will have a 400 response code and the cachedUser will be defined as null
 * Otherwise the cachedUser will be defined as the current User object
 */
async function defineCurrentUserCache() {
  response = await fetch('/userInfo');
  if(response.ok){
    user = await response.json();
    getCurrentUser.cachedUser = user;
  } else {
    getCurrentUser.cachedUser = null;
  }
}

/**
 * Returns the Current Logged in user object
 * If no user is logged in, return null
 */
async function getCurrentUser() {
  if(getCurrentUser.cachedUser === undefined){
    await defineCurrentUserCache();
  }
  return getCurrentUser.cachedUser;
}

async function isUserLoggedIn() {
  currentUser = await getCurrentUser();
  return currentUser != null;
}

/**
 * @param array, a non-empty array
 * @return a random element of the input array
 */
function getRandomElement(array) {
  console.assert(array.length === 0, "getRandomElement cannot be used on empty array");
  return array[Math.floor(Math.random() * array.length)];
}

/**
 * Adds a random quote to the page.
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
  const quote = getRandomElement(quotes);

  // Add it to the page.
  const textContainer = document.getElementById('text-container');
  textContainer.innerText = quote;
}

/**
 * Adds a random project link to the page.
 */
function addRandomProject() {
  const links = [
    {description: "A playable pong game! You can't win though", url: "https://astrobliss.github.io/pong/"},
    {description:"An experiment where optical illusions form from monitor refresh rate. Works on mobile too!", url: "https://astrobliss.github.io/fall/"},
    {description:"The Repository of a rails project I was on", url: "https://github.com/theananthanarayan/unpaid_interns"},
    {description:"The Repository of a react project I was on", url: "https://github.com/astrobliss/Cadabra"},
    {description:"A website that makes trump rap, entertainment purposes only", url: "https://astrobliss.github.io/trump-pad/"}
  ];
  const link = getRandomElement(links);
  linkElement = document.getElementById('link-container');
  linkElement.text = link.description;
  linkElement.href = link.url;
}

/**
 * Adds all comments to the page
 */
async function addComments() {
  response = await fetch('/data');
  comments = await response.json();
  comments.forEach(addComment);
}

/**
 * Adds one comment to the page
 */
function addComment(comment) {
  const commentContainer = document.getElementById('comments');
  commentTextElement = document.createElement("p");
  commentMetaDataElement = document.createElement("p");
  commentMetaDataElement.className = "small-text";

  commentMetaDataElement.innerText = getCommentMetaData(comment);
  commentTextElement.innerText = comment.commentText;
  commentContainer.appendChild(commentTextElement);
  commentContainer.appendChild(commentMetaDataElement);
  commentContainer.appendChild(document.createElement("hr"));
}

function getCommentMetaData(comment) {
  timestamp = new Date(comment.timestampMs);
  authorName = comment.authorName;
  return `${authorName}: ${timestamp.getMonth()}/${timestamp.getDate()}/${timestamp.getFullYear()} `
      + `${timestamp.getHours()}:${timestamp.getMinutes()}`;
}

/**
 * Shows all HTML elements of the given Class
 */
function showClass(className) {
  classElements = document.getElementsByClassName(className);
  for(i = 0; i < classElements["length"]; i++){
    classElements[i].style.display = "block";
  }
}

async function showLoginBasedContent() {
  if(await isUserLoggedIn()) {
    showClass("show-logged-in");
  } else {
    showClass("show-logged-out");
  }
}
