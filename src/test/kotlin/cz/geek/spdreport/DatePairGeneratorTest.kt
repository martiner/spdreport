package cz.geek.spdreport

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import java.time.LocalTime

class DatePairGeneratorTest : FreeSpec({

    "Should generate workday" {
        DatePairGenerator.generate(
            LocalDateTime.of(2023, 8, 30, 0, 0),
            LocalDateTime.of(2023, 8, 31, 0, 0)
        ).let {
            assertSoftly(it) {
                shouldHaveSize(2)
                assertSoftly(first()) {
                    first shouldBe LocalDateTime.of(2023, 8, 30, 0, 0)
                    second shouldBe LocalTime.of(9, 0)
                }
                assertSoftly(last()) {
                    first shouldBe LocalDateTime.of(2023, 8, 30, 17, 0)
                    second shouldBe LocalTime.of(0, 0)
                }
            }
        }
    }

    "Should generate morning" - {
        DatePairGenerator.generate(
            LocalDateTime.of(2023, 8, 30, 0, 0),
            LocalDateTime.of(2023, 8, 30, 8, 0)
        ).let {
            assertSoftly(it) {
                shouldHaveSize(1)
                assertSoftly(first()) {
                    first shouldBe LocalDateTime.of(2023, 8, 30, 0, 0)
                    second shouldBe LocalTime.of(8, 0)
                }
            }
        }
    }

    "Should generate half morning" {
        DatePairGenerator.generate(
            LocalDateTime.of(2023, 8, 30, 6, 0),
            LocalDateTime.of(2023, 8, 30, 8, 0)
        ).let {
            assertSoftly(it) {
                shouldHaveSize(1)
                assertSoftly(first()) {
                    first shouldBe LocalDateTime.of(2023, 8, 30, 6, 0)
                    second shouldBe LocalTime.of(8, 0)
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
            DatePairGenerator.generate(start, end).let {
                assertSoftly(it) {
                    shouldHaveSize(1)
                    assertSoftly(first()) {
                        first shouldBe LocalDateTime.of(2023, 8, 30, 0, 0)
                        second shouldBe LocalTime.of(9, 0)
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
            DatePairGenerator.generate(start, end
            ).let {
                assertSoftly(it) {
                    shouldHaveSize(1)
                    assertSoftly(first()) {
                        first shouldBe LocalDateTime.of(2023, 8, 30, 17, 0)
                        second shouldBe LocalTime.of(0, 0)
                    }
                }
            }
        }
    }

    "Should generate evening" {
        DatePairGenerator.generate(
            LocalDateTime.of(2023, 8, 30, 20, 0),
            LocalDateTime.of(2023, 8, 31, 0, 0)
        ).let {
            assertSoftly(it) {
                shouldHaveSize(1)
                assertSoftly(first()) {
                    first shouldBe LocalDateTime.of(2023, 8, 30, 20, 0)
                    second shouldBe LocalTime.of(0, 0)
                }
            }
        }
    }

    "Should generate half evening" {
        DatePairGenerator.generate(
            LocalDateTime.of(2023, 8, 30, 20, 0),
            LocalDateTime.of(2023, 8, 30, 22, 0)
        ).let {
            assertSoftly(it) {
                shouldHaveSize(1)
                assertSoftly(first()) {
                    first shouldBe LocalDateTime.of(2023, 8, 30, 20, 0)
                    second shouldBe LocalTime.of(22, 0)
                }
            }
        }
    }

})
