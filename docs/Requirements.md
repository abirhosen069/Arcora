# ArcOra - Next Generation Arc Wallet

## Project Overview

Build a production-grade Android wallet application called **ArcOra** for the Arc Testnet Network.(Only Testnet Phase)

The wallet should not resemble a traditional crypto wallet like MetaMask. Instead, it should feel like a modern fintech application (Cash App, Revolut, Venmo, Apple Wallet) powered by Arc's stablecoin-native infrastructure.

The primary goal is to hide blockchain complexity and provide a simple, consumer-friendly financial experience.

---

# Core Product Vision

ArcOra is a:

* Stablecoin wallet
* Payments application
* Smart account wallet
* AI-assisted financial platform
* Agent economy gateway

Users should never need to understand:

* Gas fees
* RPC endpoints
* Chain IDs
* Wallet addresses
* Bridging mechanics
* Transaction hashes

The app should abstract these concepts whenever possible.

---

# Platform

Target Platform:

* Android
* Kotlin
* Jetpack Compose

Recommended Architecture:

* MVVM
* Clean Architecture
* Repository Pattern
* Dependency Injection (Hilt)
* Coroutines
* StateFlow

Backend:

* Node.js or NestJS
* PostgreSQL
* Redis
* WebSocket support

Blockchain Layer:

* Arc Network
* Arc App Kit
* EVM-compatible smart accounts
* Account Abstraction providers

---

# Core Features

## 1. Smart Wallet Creation

Support:

* Email signup
* Google login
* Passkeys
* Biometric authentication

Requirements:

* Automatically create smart accounts
* No seed phrase required during onboarding
* Optional wallet export

User Flow:

Create Account
→ Verify Email
→ Create Smart Wallet
→ Wallet Ready

---

## 2. Home Dashboard

Display:

* Total Portfolio Value
* Available USDC
* Recent Activity
* Pending Requests
* Notifications

Quick Actions:

* Send
* Receive
* Request
* Pay
* Bridge

Design should resemble a modern banking application.

---

## 3. USDC Payments

Users can:

* Send USDC
* Receive USDC
* Request payments
* View receipts

Support:

* Wallet address
* Username
* QR code

Payment Flow:

Select Recipient
→ Enter Amount
→ Confirm
→ Biometric Approval
→ Transaction Sent

---

## 4. Username System

Every user receives a unique username.

Examples:

@alex
@john
@sarah

Users should never need to copy wallet addresses.

Database Requirements:

* Username uniqueness
* Profile records
* Public profile metadata

---

## 5. QR Payments

Generate:

* Personal QR
* Payment Request QR
* Merchant QR

Support:

* Static QR
* Dynamic QR

---

## 6. Cross-Chain Asset View

Using Arc App Kit:

Aggregate balances from:

* Arc
* Ethereum
* Base
* Polygon
* Solana

Display:

Total Balance

Arc: $500
Base: $1000
Ethereum: $1500

Users see one portfolio.

---

## 7. One-Tap Bridge

Users can move assets into Arc.

Flow:

Select Source Chain
→ Select Asset
→ Select Amount
→ Confirm

The application handles all routing automatically.

---

## 8. AI Wallet Assistant

Implement a conversational assistant.

Examples:

"Send 50 USDC to Alex"

"Request 100 USDC from Sarah"

"Move all my funds to Arc"

"Show my spending this month"

Assistant Responsibilities:

* Parse intent
* Validate transactions
* Generate confirmation screens
* Execute actions

Never execute transactions without explicit confirmation.

---

## 9. Transaction Inbox

Create an inbox experience similar to email.

Examples:

John requested 20 USDC

Coffee Shop refunded 5 USDC

Bridge completed

Payment received

Users can approve or reject requests.

---

## 10. Real-Time Notifications

Support:

* Payment received
* Payment sent
* Request received
* Bridge completed
* Subscription charged

Use push notifications and WebSockets.

---

## 11. Merchant Payments

Merchant Accounts should support:

* QR checkout
* Payment links
* Transaction history
* Customer receipts

Merchant Dashboard:

Daily Volume
Weekly Volume
Monthly Volume

---

## 12. Subscription Payments

Users can create recurring payments.

Examples:

Netflix
15 USDC/month

AI Agent
20 USDC/month

Features:

* Pause
* Cancel
* Renew
* Notifications

---

## 13. AI Agent Wallets

Users can create wallets for AI agents.

Agent Profile:

Name
Description
Wallet Address
Monthly Budget

Permissions:

* Spending Limit
* Allowed Transactions
* Approval Rules

---

## 14. Agent Marketplace

Users can discover and subscribe to AI agents.

Categories:

Research
Coding
Marketing
Trading
Operations

Features:

* Ratings
* Reviews
* Pricing
* Subscription Management

---

## 15. Reputation System

Every user has a reputation profile.

Metrics:

Account Age
Transactions Completed
Merchant Status
Verification Status

Profile Example:

Alex

Verified User
Reputation Score: 94

Transactions: 1500

---

## 16. Compliance Layer

Integrate compliance services available in the Arc ecosystem.

Support:

* Wallet screening
* Risk scoring
* Transaction monitoring

High-risk transactions should trigger warnings.

---

# Security Requirements

Implement:

* Encrypted local storage
* Hardware-backed keystore
* Passkeys
* Biometric authentication
* Device binding
* Session management
* Transaction confirmation screens

Never expose private keys.

---

# UI/UX Requirements

Style:

* Modern fintech
* Minimal
* Premium
* Fast

Inspirations:

* Coinbase Wallet
* Base App
* Revolut
* Cash App
* Apple Wallet

Avoid:

* Technical blockchain terminology
* Complex settings
* Developer-focused interfaces

---

# MVP Release Scope

Phase 1:

* Smart Wallet Creation
* USDC Wallet
* Username Payments
* QR Payments
* Transaction History
* Notifications
* One-Tap Bridge

Phase 2:

* AI Assistant
* Merchant Payments
* Subscription Management

Phase 3:

* Agent Wallets
* Agent Marketplace
* Reputation System

Generate a complete technical architecture, database schema, API design, Android project structure, user flows, state management plan, and implementation roadmap before writing code.
