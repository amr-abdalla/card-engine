# CardEngine

CardEngine is an expandable Java-based framework for building creature-centric card games, inspired by games such as Yu-Gi-Oh! and Pokémon TCG.

## Goals

- Provide a generic, reusable core for turn-based card games
- Decouple game rules from card logic via a flexible effect system
- Support deterministic gameplay for testing and simulations
- Showcase clean software design and extensibility

## Tools & Technologies
- Java (JDK 21)
- JUnit 5


##  Core Features
### Cards
- Creatures Cards Have attack and health values and can perform attacks and trigger effects
- Helper Cards Do not attack directly but can apply effects to other cards or duelists
### Effects System
- Generic and extensible Effect architecture
- Effects can target other cards or duelists and Interact with the game through a shared Game Context
- Designed to allow new effect types without modifying existing game logic
### Duelists
- Composed of: Deck, Hand, Zones (Creature or Helper), Life points
### Game
- Manages: Turn order, Active duelist, Game context shared across systems

## Testing
- Unit tests written using JUnit 5
- Focus on: Card behavior, Duelist state transitions, Deterministic gameplay outcomes

## TODO
- Add tests for duelist defeat conditions
- Implement a Game.update() loop to drive turn progression
- Extend the effects system to support global game state changes, such as Skipping turns.
- Add more example cards and effects
- Improve documentation and usage examples
