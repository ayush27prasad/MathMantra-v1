package com.zendalona.mathmantra.domain

data class Lesson(val id: String, val title: String, val symbol: String, val pages: List<String>)

object Lessons {
    val all: List<Lesson> = listOf(
        Lesson(
            id = "zero",
            title = "What is Zero?",
            symbol = "0",
            pages = listOf(
                "A long time ago in India, there was a very smart person named Aryabhatta. He loved learning about the stars and numbers. Aryabhatta did something very important for all of us: he helped people understand the number zero.",
                "Imagine you have a few sweets, but then you give them all away. You don't have any sweets left, right? Aryabhatta showed everyone that we can use the number zero to show that there's nothing left. Before Aryabhatta, people didn't have a way to write down 'zero' or 'nothing,' so his idea was really special.",
                "Aryabhatta's discovery of zero was like finding a missing piece of a puzzle. It helped people count and do math better. Thanks to him, we can do things like counting, adding, and even using technology like computers, all because we understand zero. Aryabhatta's ideas are still very important today, and we're grateful for his great work in India!",
                "Zero is a special number. It means nothing. When we have zero cookies, it means we have no cookies at all.",
                "Zero is also like a superhero in math! It helps us count, even when there's nothing.",
                "For example, when we start counting, we say zero, one, two, three. Zero is the start.",
                "And if you add zero to any number, that number stays the same.",
                "Zero is cool because it helps us know when there is nothing.",
            ),
        ),
        Lesson(
            id = "addition",
            title = "Addition",
            symbol = "+",
            pages = listOf(
                "A long time ago, people wanted to find out how to combine things. Imagine you have two apples and then you get two more apples. Now you have four apples! This is called addition, and it's a way to find out how many things you have when you put them together.",
                "Addition is like making a bigger group. When you have a few toys and then get some more, you can count them all together using addition. It's like a magic trick that shows us the total number of things we have.",
                "Addition helps us in our daily lives. When you count your toys, candies, or even books, you’re using addition. It makes counting easier and faster.",
                "Thanks to addition, we can easily find out the total when we bring things together. It's a simple yet powerful tool we use every day!",
            ),
        ),
        Lesson(
            id = "subtraction",
            title = "Subtraction",
            symbol = "−",
            pages = listOf(
                "Imagine you have five balloons, but then one flies away. Now you have four left. This is called subtraction, and it's how we find out how many things are left when we take some away.",
                "Subtraction is like taking things out of a group. When you share your cookies with friends, you can use subtraction to see how many you have left. It's a way to see what remains after giving some away.",
                "Subtraction is very helpful in our daily lives. Whether you’re counting how many candies you’ve eaten or how many toys you’ve given away, subtraction helps you know what’s left.",
                "Thanks to subtraction, we can figure out what happens when we take things away. It’s a simple and useful way to keep track of what we still have.",
            ),
        ),
        Lesson(
            id = "multiplication",
            title = "Multiplication",
            symbol = "×",
            pages = listOf(
                "Once upon a time, people needed a faster way to add the same number many times. Imagine you have three bags, and each bag has four apples. To find out how many apples you have in total, you can use multiplication!",
                "Multiplication is like adding the same number again and again, but much faster. Instead of saying four plus four plus four, you can simply say three times four, which gives you twelve.",
                "Multiplication helps us when we need to group things quickly. Whether you're arranging chairs or counting stars in the sky, multiplication makes it easy to find out the total.",
                "Thanks to multiplication, we can solve big counting problems in a snap. It's like a shortcut that makes math easier!",
            ),
        ),
        Lesson(
            id = "division",
            title = "Division",
            symbol = "÷",
            pages = listOf(
                "Imagine you have twelve cookies, and you want to share them equally with three friends. How many cookies will each friend get? This is where division comes in!",
                "Division is like sharing things equally among groups. It helps us find out how many things each person gets when we split them up. In our example, twelve cookies divided by three friends means each friend gets four cookies.",
                "Division is very useful in our daily lives. When you're sharing candies, books, or toys, division helps you make sure everyone gets the same amount.",
                "Thanks to division, we can split things evenly and fairly. It's a handy tool for making sure everyone gets their fair share!",
            ),
        ),
        Lesson(
            id = "percentage",
            title = "Percentage",
            symbol = "%",
            pages = listOf(
                "Long ago, people needed a way to talk about parts of a whole. Imagine you have a big pizza with 100 slices, and you eat 25 slices. You ate 25% of the pizza!",
                "Percentage is like a way to describe parts of something using the number 100. If you have 100 candies and eat 10, you ate 10% of the candies. It helps us understand how big or small a part is compared to the whole.",
                "Percentage is very helpful in daily life. Whether you're shopping, studying, or looking at scores, percentage tells you how much of something you have or need.",
                "Thanks to percentage, we can easily compare different amounts. It's a simple way to understand parts of a whole in everyday situations!",
            ),
        ),
        Lesson(
            id = "time",
            title = "Time",
            symbol = "◷",
            pages = listOf(
                "A long time ago, people needed a way to measure how long things take. Imagine you’re playing a game, and you want to know how long you’ve been playing. This is where time comes in!",
                "Time is like a way to measure how long things last. We use clocks to see how many hours, minutes, or seconds have passed. It helps us know when to start and stop doing things.",
                "Time is very important in our daily lives. It helps us wake up in the morning, go to school, play, and even know when it's time to sleep.",
                "Thanks to time, we can organize our day and make sure we do everything we need to. It's a special tool that helps us manage our day-to-day activities!",
            ),
        ),
    )

    fun byId(id: String): Lesson = all.first { it.id == id }
}
