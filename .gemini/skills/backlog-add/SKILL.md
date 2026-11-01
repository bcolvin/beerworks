---
name: backlog-add
description: Quickly append a new feature idea or task to BACKLOG.md for future review and Spec-Driven Development breakdown. Use when the user asks to "add to backlog", "save an idea", "add a task", or record a future feature idea.
---

# Backlog Add Skill

Quickly record new ideas, tasks, or feature concepts into `BACKLOG.md` without requiring a full feature specification upfront.

## Process

1. **Extract Idea**: Identify the title and brief summary of the user's idea or task.
2. **Locate `BACKLOG.md`**: Check if `BACKLOG.md` exists at the root of the project. If it doesn't exist, create it.
3. **Append Idea**:
   - Insert a new checkbox entry under the `## 💡 Unsorted / Fresh Ideas` section in `BACKLOG.md`:
     `- [ ] **[Idea Title]**: [Brief description or notes]`
4. **Confirm**:
   - Briefly confirm to the user that the item has been saved to `BACKLOG.md`.
   - Remind them that when they are ready to flush it out into a feature, they can say *"Use the skill add-feature"* or *"Break down [Idea Title] into a feature spec"*.
