package cz.geek.spdreport.service

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DateItemGeneratorTest : FreeSpec({

    "Should generate workday" {
        DateItemGenerator.generate(
            LocalDateTime.of(2023, 8, 30, 0, 0),
            LocalDateTime.of(2023, 8, 31, 0, 0)
        ).let {
            assertSoftly(it) {
                shouldHaveSize(2)
                assertSoftly(first()) {
                    day shouldBe LocalDate.of(2023, 8, 30)
                    start shouldBe LocalTime.of(0, 0)
                    end shouldBe LocalTime.of(9, 0)
                }
                assertSoftly(last()) {
                    day shouldBe LocalDate.of(2023, 8, 30)
                    start shouldBe LocalTime.of(17, 0)
                    end shouldBe LocalTime.of(0, 0)
                }
            }
        }
    }

    "Should generate morning" - {
        DateItemGenerator.generate(
            LocalDateTime.of(2023, 8, 30, 0, 0),
            LocalDateTime.of(2023, 8, 30, 8, 0)
        ).let {
            assertSoftly(it) {
                shouldHaveSize(1)
                assertSoftly(first()) {
                    day shouldBe LocalDate.of(2023, 8, 30)
                    start shouldBe LocalTime.of(0, 0)
                    end shouldBe LocalTime.of(8, 0)
                }
            }
        }
    }

    "Should generate half morning" {
        DateItemGenerator.generate(
            LocalDateTime.of(2023, 8, 30, 6, 0),
            LocalDateTime.of(2023, 8, 30, 8, 0)
        ).let {
            assertSoftly(it) {
                shouldHaveSize(1)
                assertSoftly(first()) {
                    day shouldBe LocalDate.of(2023, 8, 30)
                    start shouldBe LocalTime.of(6, 0)
                    end shouldBe LocalTime.of(8, 0)
                }
            }
        }
    }

    "Should generate till noon" - {
        withData(
            LocalDateTime.of(2023, 8, 30, 0, 0) to
            LocalDateTime.of(2023, 8, 30, 9, 0),
            LocalDateTime.of(2023, 8, 30, 0, 0) to
            LocalDateTime.of(2023, 8, 30, 12, 0),
            LocalDateTime.of(2023, 8, 30, 0, 0) to
            LocalDateTime.of(2023, 8, 30, 17, 0),
        ) { (start, end) ->
            DateItemGenerator.generate(start, end).let {
                assertSoftly(it) {
                    shouldHaveSize(1)
                    assertSoftly(first()) {
                        day shouldBe LocalDate.of(2023, 8, 30)
                        this.start shouldBe LocalTime.of(0, 0)
                        this.end shouldBe LocalTime.of(9, 0)
                    }
                }
            }
        }
    }

    "Should generate afternoon" - {
        withData(
            LocalDateTime.of(2023, 8, 30, 9, 0) to
            LocalDateTime.of(2023, 8, 31, 0, 0),
            LocalDateTime.of(2023, 8, 30, 14, 0) to
            LocalDateTime.of(2023, 8, 31, 0, 0),
            LocalDateTime.of(2023, 8, 30, 17, 0) to
            LocalDateTime.of(2023, 8, 31, 0, 0),
        ) { (start, end) ->
            DateItemGenerator.generate(start, end
            ).let {
                assertSoftly(it) {
                    shouldHaveSize(1)
                    assertSoftly(first()) {
                        day shouldBe LocalDate.of(2023, 8, 30)
                        this.start shouldBe LocalTime.of(17, 0)
                        this.end shouldBe LocalTime.of(0, 0)
                    }
                }
            }
        }
    }

    "Should generate evening" {
        DateItemGenerator.generate(
            LocalDateTime.of(2023, 8, 30, 20, 0),
            LocalDateTime.of(2023, 8, 31, 0, 0)
        ).let {
            assertSoftly(it) {
                shouldHaveSize(1)
                assertSoftly(first()) {
                    day shouldBe LocalDate.of(2023, 8, 30)
                    start shouldBe LocalTime.of(20, 0)
                    end shouldBe LocalTime.of(0, 0)
                }
            }
        }
    }

    "Should generate half evening" {
        DateItemGenerator.generate(
            LocalDateTime.of(2023, 8, 30, 20, 0),
            LocalDateTime.of(2023, 8, 30, 22, 0)
        ).let {
            assertSoftly(it) {
                shouldHaveSize(1)
                assertSoftly(first()) {
                    day shouldBe LocalDate.of(2023, 8, 30)
                    start shouldBe LocalTime.of(20, 0)
                    end shouldBe LocalTime.of(22, 0)
                }
            }
        }
    }

    "Should generate many without stack overflow" {
        DateItemGenerator.generate(
            LocalDateTime.of(2023, 1, 1, 0, 0),
            LocalDateTime.of(2053, 12, 31, 23, 0)
        )
    }

})
