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

package controllers

import controllers.predicates.AuthorisedAction

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.SubmittedGiftAidService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

class SubmittedGiftAidController @Inject()(submittedGiftAidService: SubmittedGiftAidService,
                                             cc: ControllerComponents,
                                             authorisedAction: AuthorisedAction)
                                            (implicit ec: ExecutionContext) extends BackendController(cc){

  def getSubmittedGiftAid(nino: String, taxYear:Int): Action[AnyContent] = authorisedAction.async { implicit user =>
    submittedGiftAidService.getSubmittedGiftAid(nino,taxYear).map{
      case Right(giftAidModel) => Ok(Json.toJson(giftAidModel))
      case Left(errorModel) => Status(errorModel.status)(errorModel.toJson)
    }
  }

}
