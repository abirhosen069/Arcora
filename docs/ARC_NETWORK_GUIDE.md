ARC TESTNET BUILDER GUIDE
=========================

Version: June 2026
Sources:
- https://docs.arc.io/app-kit
- https://docs.arc.io/build

--------------------------------------------------
1. WHAT IS ARC?
--------------------------------------------------

Arc is a blockchain developer platform focused on stablecoin-based
financial applications.

Arc provides:

- Stablecoin-native infrastructure
- Fast settlement
- Cross-chain payments
- Smart contract deployment
- Unified liquidity experiences
- Developer tooling integrations

Primary use cases:

- P2P Payments
- eCommerce Checkout
- Stablecoin FX
- Agentic Economy Applications

Arc supports both:

1. Direct smart contract development
2. App Kit SDK-based financial workflows

--------------------------------------------------
2. TWO WAYS TO BUILD ON ARC
--------------------------------------------------

A) Smart Contract Development

Deploy Solidity smart contracts directly onto Arc Testnet.

Use when:

- Building DeFi protocols
- Custom token systems
- NFT applications
- Onchain automation
- Custom business logic

B) App Kit Development

Use Circle's App Kit SDK.

Use when building:

- Payments
- Stablecoin transfers
- Cross-chain bridging
- Token swaps
- Unified balances

App Kit abstracts away complex protocol logic.

--------------------------------------------------
3. ARC APP KIT OVERVIEW
--------------------------------------------------

App Kit is the primary SDK for building financial applications on Arc.

Main capabilities:

1. Send
2. Bridge
3. Swap
4. Unified Balance

Benefits:

- Single SDK interface
- Multi-chain support
- Type-safe APIs
- Built-in protocol abstraction
- Fee collection support
- Minimal configuration
- Cross-chain composability

Supported wallet ecosystems:

- Viem
- Ethers
- Solana Web3
- Circle Wallets

--------------------------------------------------
4. INSTALLATION
--------------------------------------------------

Full SDK:

npm install @circle-fin/app-kit
npm install @circle-fin/adapter-viem-v2 viem

Alternative adapters:

Ethers:
npm install @circle-fin/adapter-ethers-v6 ethers

Solana:
npm install @circle-fin/adapter-solana-kit @solana/kit @solana/web3.js

Circle Wallets:
npm install @circle-fin/adapter-circle-wallets

--------------------------------------------------
5. CORE APP KIT CAPABILITIES
--------------------------------------------------

--------------------------------------------------
5.1 SEND
--------------------------------------------------

Purpose:
Transfer tokens between wallets on the same blockchain.

Example:

await kit.send({
  from: {
    adapter,
    chain: "Arc_Testnet"
  },
  to: "RECIPIENT_ADDRESS",
  amount: "1.00",
  token: "USDC"
});

Use cases:

- Wallet transfers
- User payouts
- Rewards
- Payroll systems
- Merchant settlements

--------------------------------------------------
5.2 BRIDGE
--------------------------------------------------

Purpose:
Move USDC across blockchains.

Built on:
Circle CCTP

App Kit abstracts:

- Burn
- Attestation
- Mint

Example:

await kit.bridge({
  from: {
    adapter: viemAdapter,
    chain: "Ethereum_Sepolia"
  },
  to: {
    adapter: viemAdapter,
    chain: "Arc_Testnet"
  },
  amount: "1.00"
});

Use cases:

- Funding Arc from other chains
- Cross-chain payments
- Liquidity migration

Important:

Bridge currently focuses on USDC.

--------------------------------------------------
5.3 SWAP
--------------------------------------------------

Purpose:
Swap tokens on the same blockchain.

Arc Testnet is special because:

Arc Testnet supports Swap while most testnets do not.

Supported testnet swap assets:

- USDC
- EURC
- cirBTC

Example:

await kit.swap({
  from: {
    adapter: viemAdapter,
    chain: "Arc_Testnet"
  },
  tokenIn: "USDC",
  tokenOut: "EURC",
  amountIn: "1.00",
  config: {
    kitKey: process.env.KIT_KEY
  }
});

Requirements:

- Free Kit Key from Circle Console

Use cases:

- Stablecoin FX
- Treasury management
- Currency conversion

--------------------------------------------------
5.4 UNIFIED BALANCE
--------------------------------------------------

Purpose:

Create one spendable balance from funds deposited across multiple chains.

Example:

Deposit from Base:

await kit.unifiedBalance.deposit({
  from: {
    adapter: viemAdapter,
    chain: "Base_Sepolia"
  },
  amount: "1.00",
  token: "USDC"
});

Spend on Arc:

await kit.unifiedBalance.spend({
  from: {
    adapter: viemAdapter
  },
  amountIn: "1.50",
  to: {
    adapter: viemAdapter,
    chain: "Arc_Testnet",
    recipientAddress: "0xRecipient"
  }
});

Supported asset:

- USDC only

Use cases:

- Cross-chain wallets
- Treasury aggregation
- Global payment systems

--------------------------------------------------
6. ARC TESTNET CAPABILITIES
--------------------------------------------------

Arc Testnet supports:

SEND:
✓ Supported

BRIDGE:
✓ Supported

SWAP:
✓ Supported

UNIFIED BALANCE:
✓ Supported

Arc Testnet is currently the only testnet with Swap support.

--------------------------------------------------
7. CHAIN IDENTIFIER
--------------------------------------------------

Use:

Arc_Testnet

Example:

chain: "Arc_Testnet"

or

BridgeChain.Arc_Testnet

Identifiers are case-sensitive.

--------------------------------------------------
8. SUPPORTED TOKEN ALIASES
--------------------------------------------------

App Kit supports aliases:

USDC
EURC
USDT
USDe
DAI
PYUSD
cirBTC
NATIVE

Arc Testnet swap support:

USDC
EURC
cirBTC

--------------------------------------------------
9. COMMON BUILDING FLOWS
--------------------------------------------------

--------------------------------------------------
Flow 1: Cross-Chain Deposit
--------------------------------------------------

User deposits USDC on Base Sepolia.

↓

Bridge to Arc Testnet.

↓

Spend on Arc.

Uses:

- Bridge
- Unified Balance

--------------------------------------------------
Flow 2: Stablecoin FX
--------------------------------------------------

User holds USDC.

↓

Swap USDC → EURC.

↓

Pay recipient.

Uses:

- Swap
- Send

--------------------------------------------------
Flow 3: Merchant Checkout
--------------------------------------------------

Customer pays.

↓

Bridge if necessary.

↓

Swap if necessary.

↓

Merchant receives settlement.

Uses:

- Bridge
- Swap
- Send

--------------------------------------------------
Flow 4: Global Wallet
--------------------------------------------------

User funds wallet from:

- Ethereum
- Base
- Solana

↓

Unified Balance aggregates liquidity.

↓

User spends on Arc.

Uses:

- Unified Balance

--------------------------------------------------
10. SMART CONTRACT DEVELOPMENT
--------------------------------------------------

Arc supports EVM-compatible smart contracts.

Typical workflow:

1. Connect wallet
2. Connect RPC
3. Deploy contract
4. Verify deployment
5. Interact with contract
6. Monitor events

Common contract types:

- ERC20
- ERC721
- ERC1155

--------------------------------------------------
11. DEVELOPER TOOLING ECOSYSTEM
--------------------------------------------------

Node Providers:

- Alchemy
- QuickNode
- Blockdaemon
- dRPC

Indexers:

- Envio
- Goldsky
- The Graph
- Thirdweb

Account Abstraction:

- Smart wallets
- Paymasters
- Session keys

Compliance:

- Elliptic
- TRM Labs

--------------------------------------------------
12. RECOMMENDED APP ARCHITECTURE
--------------------------------------------------

Frontend

- Next.js
- React
- Wallet connection

↓

App Kit SDK Layer

- Send
- Bridge
- Swap
- Unified Balance

↓

Arc Testnet

↓

Optional Services

- Circle Wallets
- Indexers
- Compliance Providers

--------------------------------------------------
13. BEST PRACTICES
--------------------------------------------------

1. Use App Kit whenever possible.

2. Use Unified Balance for multi-chain UX.

3. Use token aliases instead of hardcoding addresses.

4. Configure custom RPC endpoints.
   Public RPCs may be rate limited.

5. Estimate fees before Unified Balance spending.

6. Use Arc Testnet swap functionality for
   stablecoin conversion testing.

7. Design around USDC as the primary asset.

8. Build composable flows:
   Bridge + Swap + Send

--------------------------------------------------
14. HIGH-VALUE PROJECT IDEAS
--------------------------------------------------

1. Stablecoin Wallet
2. Multi-chain Treasury Manager
3. Cross-chain Payroll System
4. Merchant Checkout Platform
5. Stablecoin FX Exchange
6. Subscription Billing Platform
7. Global Remittance App
8. AI Agent Payment Network
9. Escrow Marketplace
10. Payment Router

--------------------------------------------------
15. MOST IMPORTANT TAKEAWAYS
--------------------------------------------------

- Arc is optimized for stablecoin finance.
- App Kit is the fastest way to build.
- Core primitives:
  Send, Bridge, Swap, Unified Balance.
- Arc Testnet uniquely supports swaps.
- Unified Balance enables chain abstraction.
- USDC is the central asset.
- EVM smart contracts are fully supported.
- Cross-chain UX is a first-class feature.