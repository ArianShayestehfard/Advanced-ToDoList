package com.example.todolist;

public class TaskList {
    private String description;
    private String deadline;
    private boolean isDone;

    public TaskList(String description, String deadline) {
        this.description = description;
        this.deadline = deadline;
        this.isDone = false;
    }

    public String getDescription() {
        return description;
    }

    public String getDeadline() {
        return deadline;
    }

    public boolean isDone() {
        return isDone;
    }

    public void markDone() {
        isDone = true;
    }
}