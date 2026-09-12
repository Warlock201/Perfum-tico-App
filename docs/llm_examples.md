# Anti-Patterns and Examples for LLM Coding

Real-world code examples demonstrating the four principles. Each example shows what LLMs commonly do wrong and how to fix it.

## 1. Think Before Coding

### Example 1: Hidden Assumptions
**User Request:** "Add a feature to export user data"
**❌ What LLMs Do (Wrong Assumptions):** Assumes exporting ALL users, assumes file location, assumes JSON/CSV formats and fields without asking.
**✅ What Should Happen:** Clarify Scope, Format, Fields, and Volume before writing any code.

### Example 2: Multiple Interpretations
**User Request:** "Make the search faster"
**❌ What LLMs Do (Pick Silently):** Adds caching, database indexes, and async processing without asking which "faster" matters.
**✅ What Should Happen:** Break down options (Response time vs Throughput vs Perceived UX speed) and ask for the user's priority.

## 2. Simplicity First

### Example 1: Over-abstraction
**User Request:** "Add a function to calculate discount"
**❌ What LLMs Do:** Creates Abstract Base Classes, Strategy Patterns, and Config Dataclasses for a simple math operation.
**✅ What Should Happen:** Create a single, simple function (`def calculate_discount(...)`). Add complexity ONLY when actually needed later.

### Example 2: Speculative Features
**User Request:** "Save user preferences to database"
**❌ What LLMs Do:** Adds caching layers, complex validation, merge logic, and notification systems that weren't requested.
**✅ What Should Happen:** Just execute the database UPDATE query as requested.

## 3. Surgical Changes

### Example 1: Drive-by Refactoring
**User Request:** "Fix the bug where empty emails crash the validator"
**❌ What LLMs Do:** "Improves" email validation, adds username validation, changes comments, adds docstrings.
**✅ What Should Happen:** Only change the specific lines that handle empty emails.

### Example 2: Style Drift
**User Request:** "Add logging to the upload function"
**❌ What LLMs Do:** Changes quote styles, adds type hints, reformats whitespace, changes boolean return logic.
**✅ What Should Happen:** Match existing quote style, spacing, and logic patterns. Only add the logger lines.

## 4. Goal-Driven Execution

### Example 1: Vague vs. Verifiable
**User Request:** "Fix the authentication system"
**❌ What LLMs Do:** "I'll review and improve the code" (vague).
**✅ What Should Happen:** Define success criteria. "Plan: 1. Write test reproducing the bug. 2. Fix the bug so test passes. 3. Verify no regressions."

### Example 2: Test-First Verification
**User Request:** "The sorting breaks when there are duplicate scores"
**❌ What LLMs Do:** Immediately changes sort logic without confirming the bug.
**✅ What Should Happen:** First, write a test that reproduces the non-deterministic sorting. Verify it fails. Then fix the logic and verify the test passes consistently.
