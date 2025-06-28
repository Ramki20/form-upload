const { fpacPrismaClient } = require('/opt/prisma');
const { LambdaHandlerResponse, Logger } = require('/opt/utils');

let prisma = null;
const logger = new Logger();

/**
 * Creates a record in set_asd.set_asd_set_asd_rqst.
 * @param {*} event The API Gateway event with set aside data in body
 * @returns The inserted data
 */
async function handler(event) {
  const lambdaHandlerResponse = new LambdaHandlerResponse();

  try {
    const request =
      typeof event.body === 'string' ? JSON.parse(event.body) : event.body;

    logger.debug('parsed event.body: ', request);
    logger.info('loan_id: ', request.loan_id);
    logger.info('request_id: ', request.rqst_id);

    let setAsideRequestData = null;

    if (request) {
      if (!prisma) {
        prisma = await fpacPrismaClient();
      }

      const taskId = parseInt(request.task_id);
      const requestId = parseInt(request.rqst_id);
      const loanId = parseInt(request.loan_id);
      const docId = parseInt(request.doc_id);

      await prisma.$transaction(async (prisma) => {
        setAsideRequestData = await prisma.set_asd_set_asd_rqst.create({
          data: {
            task_id: taskId,
            rqst_id: requestId,
            loan_id: loanId,
            addm_dt: request.addm_dt ? new Date(request.addm_dt) : null,
            dstr_dsgt_cd: request.dstr_dsgt_cd,
            set_asd_type_cd: request.set_asd_type_cd,
            eff_dt: request.eff_dt ? new Date(request.eff_dt) : null,
            istl_dt: request.istl_dt ? new Date(request.istl_dt) : null,
            istl_set_asd_amt: parseFloat(
              request.istl_set_asd_amt.toString().replace(/,/g, '')
            ),
            istl_paid_amt: parseFloat(
              request.istl_paid_amt.toString().replace(/,/g, '')
            ),
            cre_user_nm: request.eauth_id,
            last_chg_user_nm: request.eauth_id,
            data_stat_cd: 'A',
            cre_dt: new Date(),
            last_chg_dt: new Date(),
          },
        });

        if (docId) {
          const requestDoc = await prisma.rqst_doc.create({
            data: {
              doc_id: docId,
              data_stat_cd: 'A',
              cre_user_nm: request.eauth_id,
              last_chg_user_nm: request.eauth_id,
              doc_type: {
                connect: {
                  doc_type_cd: 'OTH',
                },
              },
              rqst: {
                connect: {
                  rqst_id: requestId,
                },
              },
            },
          });
          logger.info('requestDoc:', requestDoc);

          setAsideRequestData.documentName = request.documentName;
          setAsideRequestData.documentId = request.documentId;
        }

        logger.info('setAsideRequestData in lambda: ', setAsideRequestData);

        lambdaHandlerResponse.body = { setAsideRequestData };
      });
    } else {
      lambdaHandlerResponse.body = null;
      logger.info('Request object is null');
    }
  } catch (error) {
    logger.error('Error: ', error);
    prisma = null;
    lambdaHandlerResponse.errors = [error.message];
    lambdaHandlerResponse.setError(500);
  }

  return lambdaHandlerResponse.toAPIGatewayResponse();
}

module.exports = { handler };
