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

package connectors.httpParsers

import models.{DesErrorModel, GiftAidSubmissionResponseModel}
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.PagerDutyHelper.PagerDutyKeys._
import utils.PagerDutyHelper.pagerDutyLog


object GiftAidSubmissionHttpParser extends DESParser {
  type GiftAidSubmissionResponse = Either[DesErrorModel, GiftAidSubmissionResponseModel]

  override val parserName: String = "GiftAidSubmissionHttpParser"

  implicit object CreateIncomeSourceHttpReads extends HttpReads[GiftAidSubmissionResponse] {

    override def read(method: String, url: String, response: HttpResponse): GiftAidSubmissionResponse = {
      response.status match {
        case OK =>
          Json.parse(response.body).validate[GiftAidSubmissionResponseModel].asOpt match {
            case Some(model) => Right(model)
            case None => badSuccessJsonFromDES
          }
        case INTERNAL_SERVER_ERROR =>
          pagerDutyLog(INTERNAL_SERVER_ERROR_FROM_DES, logMessage(response))
          handleDESError(response)
        case SERVICE_UNAVAILABLE =>
          pagerDutyLog(SERVICE_UNAVAILABLE_FROM_DES, logMessage(response))
          handleDESError(response)
        case BAD_REQUEST | FORBIDDEN =>
          pagerDutyLog(FOURXX_RESPONSE_FROM_DES, logMessage(response))
          handleDESError(response)
        case _ =>
          pagerDutyLog(UNEXPECTED_RESPONSE_FROM_DES, logMessage(response))
          handleDESError(response, Some(INTERNAL_SERVER_ERROR))
      }
    }

  }

}
