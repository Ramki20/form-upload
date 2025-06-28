const { fpacPrismaClient } = require('/opt/prisma')
const { handler } = require('./handler/handler')

const testEvent = {
  body: JSON.stringify({
    task_id: 1,
    rqst_id: 1228,
    loan_id: 1,
    doc_id: 1,
    addm_dt: '2025-05-15T23:40:29.419Z',
    dstr_dsgt_cd: 'aaaaa',
    set_asd_type_cd: 'bbbb',
    eff_dt: '2025-05-15T23:40:29.419Z',
    istl_dt: '2025-05-15T23:40:29.419Z',
    istl_set_asd_amt: 3.5,
    istl_paid_amt: 3.5,
    eauth_id: 'user_name'
  }),
}

const badEvent = {
  body: 'invalid_json',
}

jest.mock('/opt/prisma', () => ({
  fpacPrismaClient: jest.fn().mockResolvedValue({
    rqst_loan: {
      findMany: jest.fn().mockResolvedValue([
        {
          rqst_id: 1,
          loan_id: 1,
          data_stat_cd: 'A',
          cre_dt: '2025-05-15T23:40:29.419Z',
        },
      ]),
    },
    set_asd_set_asd_rqst: {
      create: jest.fn().mockResolvedValue({
        task_id: 1,
        rqst_id: 1,
        loan_id: 1,
        addm_dt: '2025-05-15T23:40:29.419Z',
        dstr_dsgt_cd: 'aaaaa',
        set_asd_type_cd: 'bbbb',
        eff_dt: '2025-05-15T23:40:29.419Z',
        istl_dt: '2025-05-15T23:40:29.419Z',
        istl_set_asd_amt: 3.5,
        istl_paid_amt: 3.5,
        cre_user_nm: 'user_name',
        last_chg_user_nm: 'user_name',
        data_stat_cd: 'A',
        cre_dt: '2025-05-15T23:40:29.419Z',
        last_chg_dt: '2025-05-15T23:40:29.419Z',
      }),
    },
    $transaction: jest.fn().mockImplementation(async (callback) =>
      callback({
        set_asd_set_asd_rqst: {
          create: jest.fn().mockResolvedValue({
            task_id: 1,
            rqst_id: 1,
            loan_id: 1,
            addm_dt: '2025-05-15T23:40:29.419Z',
            dstr_dsgt_cd: 'aaaaa',
            set_asd_type_cd: 'bbbb',
            eff_dt: '2025-05-15T23:40:29.419Z',
            istl_dt: '2025-05-15T23:40:29.419Z',
            istl_set_asd_amt: 3.5,
            istl_paid_amt: 3.5,
            cre_user_nm: 'user_name',
            last_chg_user_nm: 'user_name',
            data_stat_cd: 'A',
            cre_dt: '2025-05-15T23:40:29.419Z',
            last_chg_dt: '2025-05-15T23:40:29.419Z',
          }),
        },
        rqst_doc: {
          create: jest.fn().mockResolvedValue({
            rqst_doc_id: 2055,
            rqst_id: 1228,
            doc_type_cd: 'OTH',
            doc_id: 1,
            doc_type_ot_desc: null,
            data_stat_cd: 'A',
            cre_dt: '2025-06-16T17:47:56.154Z',
            cre_user_nm: 'user_name',
            last_chg_dt: '2025-06-16T17:47:56.154Z',
            last_chg_user_nm: 'user_name',
            loan_id: null
          })
        }
      }),
    ),
  }),
}))

afterEach(() => {
  jest.restoreAllMocks()
  jest.clearAllMocks()
})

describe('saveSetAsideRequest lambda function', () => {
  it('returns the desired response', async () => {
    const actual = await handler(testEvent)
    const mockReturnObject = {
      body: JSON.stringify({
        setAsideRequestData: {
          task_id: 1,
          rqst_id: 1,
          loan_id: 1,
          addm_dt: '2025-05-15T23:40:29.419Z',
          dstr_dsgt_cd: 'aaaaa',
          set_asd_type_cd: 'bbbb',
          eff_dt: '2025-05-15T23:40:29.419Z',
          istl_dt: '2025-05-15T23:40:29.419Z',
          istl_set_asd_amt: 3.5,
          istl_paid_amt: 3.5,
          cre_user_nm: 'user_name',
          last_chg_user_nm: 'user_name',
          data_stat_cd: 'A',
          cre_dt: '2025-05-15T23:40:29.419Z',
          last_chg_dt: '2025-05-15T23:40:29.419Z',
        },
      }),
      statusCode: 200,
    }
    expect(actual).toMatchObject(mockReturnObject)
  })

  it('handles errors', async () => {
    const badReturn = await handler(badEvent)
    const mockReturnObject = {
      body: JSON.stringify({
        errors: ['Unexpected token \'i\', "invalid_json" is not valid JSON'],
      }),
      statusCode: 500,
    }
    expect(badReturn).toMatchObject(mockReturnObject)
  })
})
