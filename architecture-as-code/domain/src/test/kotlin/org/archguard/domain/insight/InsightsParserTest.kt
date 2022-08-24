package org.archguard.domain.insight

import org.archguard.domain.comparison.Comparison
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class InsightsParserTest {

    @Test
    fun normalDSL() {
        val tokens = InsightsParser.tokenize("""dep_name = "5" and version = "1.3.7"""")
        assertEquals(7, tokens.size)

        listOf(
            Token(type = TokenType.Identifier, value = "dep_name", start = 0, end = 8),
            Token(type = TokenType.ComparisonKind, value = "=", start = 9, end = 10),
            Token(type = TokenType.StringKind, value = "\"5\"", start = 11, end = 14),
            Token(type = TokenType.Combinator, value = "and", start = 15, end = 18),
            Token(type = TokenType.Identifier, value = "version", start = 19, end = 26),
            Token(type = TokenType.ComparisonKind, value = "=", start = 27, end = 28),
            Token(type = TokenType.StringKind, value = "\"1.3.7\"", start = 29, end = 36)
        ).forEachIndexed { index, token ->
            assertEquals(token, tokens[index])
        }
    }

    @Test
    fun uppercaseKeywords() {
        val tokens = InsightsParser.tokenize("""dep_name = "5" AND version = "1.3.7" OR a = "a" THEN b = /b/""")
        assertEquals(15, tokens.size)
    }

    @Test
    fun unpairedWrapper() {
        val tokens = InsightsParser.tokenize("dep_name = '\"`/%")

        assertEquals(7, tokens.size)
    }

    @Test
    fun stringValues() {
        val tokens = InsightsParser.tokenize("dep_name = 'log4j'")

        assertEquals(3, tokens.size)
        assertEquals(Token(type = TokenType.StringKind, value = "'log4j'", start = 11, end = 18), tokens[2])

        val tokens2 = InsightsParser.tokenize("dep_name = \"log4j\"")

        assertEquals(3, tokens2.size)
        assertEquals(Token(type = TokenType.StringKind, value = "\"log4j\"", start = 11, end = 18), tokens2[2])

        val tokens3 = InsightsParser.tokenize("dep_name = `log4j`")

        assertEquals(3, tokens3.size)
        assertEquals(Token(type = TokenType.StringKind, value = "`log4j`", start = 11, end = 18), tokens3[2])
    }

    @Test
    fun regexValue() {
        val tokens = InsightsParser.tokenize("dep_name = /log4j/")

        assertEquals(3, tokens.size)
        assertEquals(Token(type = TokenType.RegexKind, value = "/log4j/", start = 11, end = 18), tokens[2])
    }

    @Test
    fun likeValue() {
        val tokens = InsightsParser.tokenize("dep_name = @%log4j%@")

        assertEquals(3, tokens.size)
        assertEquals(Token(type = TokenType.LikeKind, value = "@%log4j%@", start = 11, end = 20), tokens[2])
    }

    @Test
    fun expression() {
        val sample = "dep_name = 'log4j'"
        val tokens = InsightsParser.tokenize(sample)

        assertEquals(3, tokens.size)
        assertEquals(Token(type = TokenType.ComparisonKind, value = "=", start = 9, end = 10), tokens[1])
    }

    @Test
    fun multipleExpr() {
        val tokens = InsightsParser.tokenize("""a = "1" and b = "2" && c = "3" or d = "4" || e = "5"""")

        assertEquals(19, tokens.size)

        assertEquals(Token(type = TokenType.Combinator, value = "and", start = 8, end = 11), tokens[3])
        assertEquals(Token(type = TokenType.Combinator, value = "&&", start = 20, end = 22), tokens[7])
        assertEquals(Token(type = TokenType.Combinator, value = "or", start = 31, end = 33), tokens[11])
        assertEquals(Token(type = TokenType.Combinator, value = "||", start = 42, end = 44), tokens[15])
    }

    @Test
    fun invalidToken() {
        val tokens = InsightsParser.tokenize("&")
        assertEquals(1, tokens.size)
        assertEquals(Token(TokenType.Unknown, value = "&", start = 0, end = 1), tokens[0])
    }

    @Test
    fun identifier() {
        val tokens = InsightsParser.tokenize("dep")
        assertEquals(1, tokens.size)
        assertEquals(Token(TokenType.Identifier, value = "dep", start = 0, end = 3), tokens[0])
    }

    @Test
    fun validQuery() {
        val query = InsightsParser.parse("dep_name = 'hello'")
        assertEquals(1, query.query.size)
        assertEquals(
            Either.Left(QueryExpression("dep_name", "hello", QueryMode.StrictMode, Comparison.Equal)), query.query[0]
        )
    }

    @Test
    fun validMultipleQuery() {
        val query = InsightsParser.parse("a = 'hello' and b = @%b%@")
        assertEquals(3, query.query.size)


        listOf<Either<QueryExpression, QueryCombinator>>(
            Either.Left(
                QueryExpression(
                    left = "a", right = "hello", queryMode = QueryMode.StrictMode, comparison = Comparison.Equal
                )
            ),
            Either.Right(QueryCombinator(value = "and", type = CombinatorType.And)),
            Either.Left(
                QueryExpression(
                    left = "b", right = "%b%", queryMode = QueryMode.LikeMode, comparison = Comparison.Equal
                )
            ),
        ).forEachIndexed { index, expected ->
            assertEquals(expected, query.query[index])
        }
    }

    @Test
    fun invalidQueryUnpaired() {
        val exception = assertThrows<java.lang.IllegalArgumentException> {
            InsightsParser.parse("a = '")
        }

        assertEquals("Input is not a valid query", exception.message)
    }

    @Test
    fun invalidQueryThatBeginWithCombinator() {
        val exception = assertThrows<java.lang.IllegalArgumentException> {
            InsightsParser.parse("and a='5'")
        }

        assertEquals("Combinator should followed by a full expression", exception.message)
    }

    @Test
    fun invalidQueryThatEndWithCombinator() {
        val exception = assertThrows<java.lang.IllegalArgumentException> {
            InsightsParser.parse("a='5' and")
        }

        assertEquals("Combinator should not presents at the end of query", exception.message)
    }

    @Test
    fun invalidQueryIdentifierIsNotPresents() {
        val exception = assertThrows<java.lang.IllegalArgumentException> {
            InsightsParser.parse("='5'")
        }

        assertEquals("Identifier is not presents", exception.message)
    }

    @Test
    fun invalidQueryComparatorIsNotPresents() {
        val exception = assertThrows<java.lang.IllegalArgumentException> {
            InsightsParser.parse("a '5'")
        }

        assertEquals("Comparator is not presents", exception.message)
    }

    @Test
    fun toQuery() {
        val queryString = InsightsParser.parse("a='a' and b=@b%@ or c!='c' && d>'d'").toSQL()

        assertEquals("WHERE a = 'a' AND b LIKE 'b%' OR c != 'c' AND d > 'd'", queryString)
    }

    @Test
    fun toQueryWithEscape() {
        val queryString = InsightsParser.parse("message = `you're welcome`").toSQL()

        assertEquals("WHERE message = 'you''re welcome'", queryString)
    }

    @Test
    fun simpleHybridQuery() {
        val query = InsightsParser.parse("message = '5' then name = /hello/")

        assertEquals(1, query.query.size)
        assertEquals(1, query.postqueries.size)
    }

    @Test
    fun simpleHybridQueryWithVersion() {
        val query = InsightsParser.parse("message = '5' then dep_version >= 1.2.0-alpha")
        assertEquals(1, query.query.size)
        assertEquals("WHERE message = '5'", query.toSQL())
        assertEquals(1, query.postqueries.size)
        assertEquals(Either.Right(VersionQuery("dep_version", ">=", "1.2.0-alpha", null)), query.postqueries[0])
    }

    @Test
    fun sqlQueryOnly() {
        val query = InsightsParser.parse("name = \"hello\"")

        assertEquals(1, query.query.size)
        assertEquals(0, query.postqueries.size)
    }

    @Test
    fun postqueryOnly() {
        val query = InsightsParser.parse("name = /hello/")

        assertEquals(0, query.query.size)
        assertEquals(1, query.postqueries.size)
        // Postquery only mode should generate sql WHERE clause like below
        assertEquals("WHERE 1=1", query.toSQL())
    }
}