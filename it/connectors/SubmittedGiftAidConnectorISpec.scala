/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import models.giftAid.{GiftAidPaymentsModel, GiftsModel, SubmittedGiftAidModel}
import models.{DesErrorBodyModel, DesErrorModel, DesErrorsBodyModel}
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import utils.IntegrationTest

class SubmittedGiftAidConnectorISpec extends IntegrationTest {

  lazy val connector: SubmittedGiftAidConnector = app.injector.instanceOf[SubmittedGiftAidConnector]

  val nino: String = "123456789"
  val taxYear: Int = 1999
  val giftAidPayments: GiftAidPaymentsModel = GiftAidPaymentsModel(
    Some(List("")), Some(12345.67), Some(12345.67), Some(12345.67), Some(12345.67), Some(12345.67)
  )
  val gifts: GiftsModel = GiftsModel(Some(List("")), Some(12345.67), Some(12345.67) , Some(12345.67))

  ".SubmittedGiftAidConnector" should {
    "return a SubmittedGiftAidModel" when {
      "all values are present" in {
        val expectedResult = SubmittedGiftAidModel(Some(giftAidPayments), Some(gifts))

        stubGetWithResponseBody(s"/income-tax/nino/$nino/income-source/charity/annual/$taxYear", OK, Json.toJson(expectedResult).toString())

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.getSubmittedGiftAid(nino, taxYear)(hc))

        result shouldBe Right(expectedResult)
      }
    }

    "DES Returns multiple errors" in {
      val expectedResult = DesErrorModel(BAD_REQUEST, DesErrorsBodyModel(Seq(
        DesErrorBodyModel("INVALID_IDTYPE","ID is invalid"),
        DesErrorBodyModel("INVALID_IDTYPE_2","ID 2 is invalid"))))

      val responseBody = Json.obj(
        "failures" -> Json.arr(
          Json.obj("code" -> "INVALID_IDTYPE",
            "reason" -> "ID is invalid"),
          Json.obj("code" -> "INVALID_IDTYPE_2",
            "reason" -> "ID 2 is invalid")
        )
      )
      stubGetWithResponseBody(s"/income-tax/nino/$nino/income-source/charity/annual/$taxYear", BAD_REQUEST, responseBody.toString())

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getSubmittedGiftAid(nino, taxYear)(hc))

      result shouldBe Left(expectedResult)
    }

    "return a Parsing error INTERNAL_SERVER_ERROR response" in {
      val invalidJson = Json.obj(
        "giftAidPayments" -> ""
      )

      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

      stubGetWithResponseBody(s"/income-tax/nino/$nino/income-source/charity/annual/$taxYear", OK, invalidJson.toString())
      implicit val hc = HeaderCarrier()
      val result = await(connector.getSubmittedGiftAid(nino, taxYear)(hc))

      result shouldBe Left(expectedResult)
    }

    "return a NO_CONTENT" in {
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

      stubGetWithResponseBody(s"/income-tax/nino/$nino/income-source/charity/annual/$taxYear", NO_CONTENT, "{}")
      implicit val hc = HeaderCarrier()
      val result = await(connector.getSubmittedGiftAid(nino, taxYear)(hc))

      result shouldBe Left(expectedResult)
    }

    "return a Bad Request" in {
      val responseBody = Json.obj(
        "code" -> "INVALID_NINO",
        "reason" -> "Nino is invalid"
      )
      val expectedResult = DesErrorModel(400, DesErrorBodyModel("INVALID_NINO", "Nino is invalid"))

      stubGetWithResponseBody(s"/income-tax/nino/$nino/income-source/charity/annual/$taxYear", BAD_REQUEST, responseBody.toString())
      implicit val hc = HeaderCarrier()
      val result = await(connector.getSubmittedGiftAid(nino, taxYear)(hc))

      result shouldBe Left(expectedResult)
    }

    "return a Not found" in {
      val responseBody = Json.obj(
        "code" -> "NOT_FOUND_INCOME_SOURCE",
        "reason" -> "Can't find income source"
      )
      val expectedResult = DesErrorModel(404, DesErrorBodyModel("NOT_FOUND_INCOME_SOURCE", "Can't find income source"))

      stubGetWithResponseBody(s"/income-tax/nino/$nino/income-source/charity/annual/$taxYear", NOT_FOUND, responseBody.toString())
      implicit val hc = HeaderCarrier()
      val result = await(connector.getSubmittedGiftAid(nino, taxYear)(hc))

      result shouldBe Left(expectedResult)
    }

    "return an Internal server error" in {
      val responseBody = Json.obj(
        "code" -> "SERVER_ERROR",
        "reason" -> "Internal server error"
      )
      val expectedResult = DesErrorModel(500, DesErrorBodyModel("SERVER_ERROR", "Internal server error"))

      stubGetWithResponseBody(s"/income-tax/nino/$nino/income-source/charity/annual/$taxYear", INTERNAL_SERVER_ERROR, responseBody.toString())
      implicit val hc = HeaderCarrier()
      val result = await(connector.getSubmittedGiftAid(nino, taxYear)(hc))

      result shouldBe Left(expectedResult)
    }

    "return a Service Unavailable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Service is unavailable"
      )
      val expectedResult = DesErrorModel(503, DesErrorBodyModel("SERVICE_UNAVAILABLE", "Service is unavailable"))

      stubGetWithResponseBody(s"/income-tax/nino/$nino/income-source/charity/annual/$taxYear", SERVICE_UNAVAILABLE, responseBody.toString())
      implicit val hc = HeaderCarrier()
      val result = await(connector.getSubmittedGiftAid(nino, taxYear)(hc))

      result shouldBe Left(expectedResult)
    }

    "return an Internal Server Error when DES throws an unexpected result" in {
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

      stubGetWithoutResponseBody(s"/income-tax/nino/$nino/income-source/charity/annual/$taxYear", NO_CONTENT)
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getSubmittedGiftAid(nino, taxYear)(hc))

      result shouldBe Left(expectedResult)
    }

    "return an Internal Server Error when DES throws an unexpected result that is parsable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Service is unavailable"
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR,  DesErrorBodyModel("SERVICE_UNAVAILABLE", "Service is unavailable"))

      stubGetWithResponseBody(s"/income-tax/nino/$nino/income-source/charity/annual/$taxYear", CONFLICT, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getSubmittedGiftAid(nino, taxYear)(hc))

      result shouldBe Left(expectedResult)
    }

    "return an Internal Server Error when DES throws an unexpected result that isn't parsable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE"
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR,  DesErrorBodyModel.parsingError)

      stubGetWithResponseBody(s"/income-tax/nino/$nino/income-source/charity/annual/$taxYear", CONFLICT, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getSubmittedGiftAid(nino, taxYear)(hc))

      result shouldBe Left(expectedResult)
    }
  }
}
